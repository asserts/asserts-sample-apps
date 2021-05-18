/*
 *  Copyright © 2020.
 *  Asserts, Inc. - All Rights Reserved
 */
package ai.asserts.sample.apps.auction;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@With
@NoArgsConstructor
@ToString
@DynamoDbBean
public class Bid {
    private String id;
    private String itemId;
    private String bidderId;
    private Double price;
    private Integer quantity;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
