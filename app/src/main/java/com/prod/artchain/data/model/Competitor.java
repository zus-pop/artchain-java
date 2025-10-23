package com.prod.artchain.data.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Competitor {
    private String userId;
    private String fullName;
    private String grade;
    private String schoolName;
}
