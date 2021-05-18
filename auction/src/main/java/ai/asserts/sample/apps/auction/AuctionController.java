/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        return ResponseEntity.ok(new Item().withId(1));
    }

    @RequestMapping(
            path = "/item/{id}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<Item> getItem(@PathVariable("id") String itemId) {
        return ResponseEntity.ok(new Item(1, "Laptop", 300D, 10000D));
    }

    @RequestMapping(
            path = "/submit-bid",
            produces = APPLICATION_JSON_VALUE,
            method = POST)
    public ResponseEntity<Bid> submitBid(@RequestBody Bid bid) {
        return ResponseEntity.ok(new Bid().withId(1));
    }

    @RequestMapping(
            path = "/bid/{id}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<Bid> getBid(@PathVariable("id") String bidId) {
        return ResponseEntity.ok(new Bid(1, 1, 1, 300.0D, 15000));
    }

    @RequestMapping(
            path = "/items",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    public ResponseEntity<List<Item>> listItems() {
        List<Item> items = new ArrayList<>();
        int numItems = (int) (500 * Math.random());
        for (int i = 0; i < numItems; i++) {
            items.add(new Item(i, "Laptop + " + i, 300D, 10000D));
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
            method = GET)
    public ResponseEntity<String> writeBidsToDynamo() {
        return ResponseEntity.ok("Wrote Bids");
    }

    List<Bid> generateBids() {
        List<Bid> bids = new ArrayList<>();
        int numBids = (int) (500 * Math.random());
        for (int i = 0; i < numBids; i++) {
            bids.add(new Bid(i, 1, i, 300.0D, 15000));
        }
        return bids;
    }
}
