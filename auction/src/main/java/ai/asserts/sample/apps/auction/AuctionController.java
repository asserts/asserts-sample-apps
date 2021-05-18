/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.valueOf;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@SuppressWarnings("unused")
@Slf4j
public class AuctionController {
    private final String dynamoTableName;
    private final String s3BucketName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuctionController(@Value("${sample.app.dynamodb.table_name}") String dynamoTableName,
                             @Value("${sample.app.s3.bucket_name}") String s3BucketName) {
        this.dynamoTableName = dynamoTableName;
        this.s3BucketName = s3BucketName;
    }

    @RequestMapping(
            path = "/create-auction",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<Auction> createAuction() {
        return ResponseEntity.ok(new Auction().withId(1));
    }

    @RequestMapping(
            path = "/add-item",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<Item> addItem(@RequestBody Item item) {
        return ResponseEntity.ok(new Item().withId("1"));
    }

    @RequestMapping(
            path = "/item/{id}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<Item> getItem(@PathVariable("id") String itemId) {
        return ResponseEntity.ok(new Item("1", "Laptop", 300D, 10000D));
    }

    @RequestMapping(
            path = "/submit-bid",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<Bid> submitBid(@RequestBody Bid bid) {
        return ResponseEntity.ok(new Bid().withId("1"));
    }

    @RequestMapping(
            path = "/bid/{id}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<Bid> getBid(@PathVariable("id") String bidId) {
        return ResponseEntity.ok(new Bid("1", "1", "1", 300.0D, 15000));
    }

    @RequestMapping(
            path = "/items",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<List<Item>> listItems() {
        List<Item> items = new ArrayList<>();
        int numItems = (int) (500 * Math.random());
        for (int i = 0; i < numItems; i++) {
            items.add(new Item(i + "", "Laptop + " + i, 300D, 10000D));
        }
        return ResponseEntity.ok(items);
    }

    @RequestMapping(
            path = "/bids",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<List<Bid>> listBids() {
        List<Bid> bids = generateBids();
        return ResponseEntity.ok(bids);
    }

    @RequestMapping(
            path = "/http/{status_code}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<String> httpStatus(@PathVariable("status_code") String statusCode) {
        try {
            return new ResponseEntity<>("Simple Status Response", valueOf(statusCode));
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server error", INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/useCPU",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<String> useCPU() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/Workbench_2021_03_11.json");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(out);
            zipOutputStream.putNextEntry(new ZipEntry("Workbench_2021_03_11.json"));
            FileCopyUtils.copy(inputStream, zipOutputStream);
            return ResponseEntity.ok(out.toString());
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server error", INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/useMemory",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<String> useMemory() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IntStream.rangeClosed(1, 10).forEach(_int -> {
                try {
                    InputStream inputStream = getClass().getResourceAsStream("/Workbench_2021_03_11.json");
                    out.write(FileCopyUtils.copyToByteArray(inputStream));
                } catch (IOException e) {
                    log.error("Error out ", e);
                }
            });
            return ResponseEntity.ok("Used Memory");
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server error", INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/writeBidsToDynamo",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<String> writeBidsToDynamo(@RequestBody String requestBody) {
        try {
            DynamoDbEnhancedClient enhancedClient = getDynamoDbEnhancedClient();

            DynamoDbTable<Bid> mappedTable = enhancedClient.table(dynamoTableName, TableSchema.fromBean(Bid.class));

            LocalDate localDate = LocalDate.parse("2020-04-07");
            LocalDateTime localDateTime = localDate.atStartOfDay();
            Instant instant = localDateTime.toInstant(ZoneOffset.UTC);


            // Create a BatchWriteItemEnhancedRequest object
            WriteBatch.Builder<Bid> batchBuilder = WriteBatch.builder(Bid.class)
                    .mappedTableResource(mappedTable);
            List<Bid> bids = generateBids();
            for (int i = 0; i < Math.min(25, bids.size()); i++) {
                batchBuilder.addPutItem(bids.get(i));
            }
            WriteBatch batch = batchBuilder.build();

            BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
                    BatchWriteItemEnhancedRequest.builder()
                            .writeBatches(batch)
                            .build();

            // Add these two items to the table
            enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
            log.info("Batch Write of bids complete");

        } catch (DynamoDbException e) {
            log.error("Dynamo table batch write failed", e);
            System.exit(1);
        }
        return ResponseEntity.ok("Wrote Bids");
    }

    @RequestMapping(
            path = "/readBidsFromDynamo",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<List<Bid>> readBidsFromDynamo() {
        DynamoDbEnhancedClient enhancedClient = getDynamoDbEnhancedClient();
        DynamoDbTable<Bid> mappedTable = enhancedClient.table(dynamoTableName, TableSchema.fromBean(Bid.class));
        return ResponseEntity.ok(mappedTable.scan().items().stream().collect(Collectors.toList()));
    }

    @RequestMapping(
            path = "/writeBidsToS3",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<String> writeBidsToS3(@RequestBody String requestBody) {
        S3Client s3Client = getS3Client();
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key("all-bids")
                .build();
        List<Bid> bids = generateBids();
        try {
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(bids);
            s3Client.putObject(
                    putRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(jsonString.getBytes(UTF_8)));
            return ResponseEntity.ok("Wrote Bids to S3");
        } catch (JsonProcessingException e) {
            log.error("Failed to write bids to S3", e);
            return new ResponseEntity<>("Internal Server error", INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(
            path = "/readBidsFromS3",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<List<Bid>> readBidsFromS3() {
        S3Client s3Client = getS3Client();
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(s3BucketName)
                .key("all-bids")
                .build();
        try {
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(getRequest);
            return ResponseEntity.ok(objectMapper.readValue(objectAsBytes.asUtf8String(), new TypeReference<List<Bid>>() {
            }));
        } catch (JsonProcessingException e) {
            log.error("Failed to write bids to S3", e);
            return new ResponseEntity<>(Collections.emptyList(), INTERNAL_SERVER_ERROR);
        }
    }

    List<Bid> generateBids() {
        List<Bid> bids = new ArrayList<>();
        int numBids = (int) (500 * Math.random());
        for (int i = 0; i < numBids; i++) {
            bids.add(new Bid(i + "", "1", i + "", 300.0D, 15000));
        }
        return bids;
    }

    private DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
//        Credentials credentials = Credentials.builder()
//                .accessKeyId("ASIAU7XAO4EFR2V6ZIRQ")
//                .secretAccessKey("/s9cveMx/s3J7Iq+jEL6kfZvrHNJCWYHUDnQJ75C")
//                .sessionToken("FwoGZXIvYXdzENf//////////wEaDEjVG4I1b24yjMM6wSK4ASKtcyVc+7wQo6Udzk8O7TspS7O7nWdKUHqNDjPqg3I3flIvoUiOPNp6/K3tnRGtwlmetlg+1GpoIn0HItHt3EtCxzIwflPRDMZb/74PSGW01hMucIgaqJn5FPbgci79zs858jdnXdlOShTzR1hsdEbSt61vaTPAEC1mnIpK/rRwuhqOOKnruvnmb+06/ES+XlmbClsFdtJP9dsewJKlS7MfCye1jcM3kDr8cJabUQ6A/cA3HWK4T3QoyYqPhQYyLTGy5QCI/KCU/KEpXIVeWplB+HOixanfoNiabxXhG1GLG1hyOI6t41sAVJD1Sg==")
//                .build();

//        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
//                credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());

        DynamoDbClient ddb = DynamoDbClient.builder()
                .region(Region.US_WEST_2)
//                .credentialsProvider(() -> sessionCredentials)
                .build();

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddb)
                .build();
        return enhancedClient;
    }

    private S3Client getS3Client() {
//        Credentials credentials = Credentials.builder()
//                .accessKeyId("ASIAU7XAO4EFR2V6ZIRQ")
//                .secretAccessKey("/s9cveMx/s3J7Iq+jEL6kfZvrHNJCWYHUDnQJ75C")
//                .sessionToken("FwoGZXIvYXdzENf//////////wEaDEjVG4I1b24yjMM6wSK4ASKtcyVc+7wQo6Udzk8O7TspS7O7nWdKUHqNDjPqg3I3flIvoUiOPNp6/K3tnRGtwlmetlg+1GpoIn0HItHt3EtCxzIwflPRDMZb/74PSGW01hMucIgaqJn5FPbgci79zs858jdnXdlOShTzR1hsdEbSt61vaTPAEC1mnIpK/rRwuhqOOKnruvnmb+06/ES+XlmbClsFdtJP9dsewJKlS7MfCye1jcM3kDr8cJabUQ6A/cA3HWK4T3QoyYqPhQYyLTGy5QCI/KCU/KEpXIVeWplB+HOixanfoNiabxXhG1GLG1hyOI6t41sAVJD1Sg==")
//                .build();

//        AwsSessionCredentials sessionCredentials = AwsSessionCredentials.create(
//                credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken());

        S3Client s3Client = S3Client.builder()
                .region(Region.US_WEST_2)
//                .credentialsProvider(() -> sessionCredentials)
                .build();

        return s3Client;
    }
}
