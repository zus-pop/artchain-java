package com.prod.artchain.data.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Round implements Serializable {
    private Integer roundId;
    private Integer contestId;
    private String table;
    private String name;
    private String startDate;
    private String endDate;
    private String submissionDeadline;
    private String resultAnnounceDate;
    private String sendOriginalDeadline;
    private String status;
    private String createdAt;
    private String updatedAt;
}
