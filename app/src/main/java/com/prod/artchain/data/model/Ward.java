package com.prod.artchain.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ward {
    private String name;
    private int code;
    private String divisionType;
    private String codename;
    private int provinceCode;

    @Override
    public String toString() {
        return name;
    }
}
