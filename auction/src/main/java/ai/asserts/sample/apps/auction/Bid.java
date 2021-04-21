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

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@With
@NoArgsConstructor
@ToString
public class Bid {
    private Integer id;
    private Integer itemId;
    private Integer bidderId;
    private Double price;
    private Integer quantity;
}
