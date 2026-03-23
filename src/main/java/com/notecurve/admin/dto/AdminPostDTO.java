package com.notecurve.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPostDTO {

    private Long id;
    private Long userId;
    private String title;
    private String userName;
    private LocalDate date;
}
