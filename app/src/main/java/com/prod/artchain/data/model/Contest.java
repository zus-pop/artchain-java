package com.prod.artchain.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Contest implements Serializable {
    private String contestId;
    private String title;
    private String description;
    private String bannerUrl;
    private int numOfAward;
    private Date startDate;
    private Date endDate;
    private String status;
    private String createdBy;
    private List<Round> rounds;
}
