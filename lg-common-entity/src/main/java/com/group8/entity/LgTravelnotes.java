package com.group8.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LgTravelnotes {

    private long notesId;
    private String notesUrl;
    private String notesTitle;
    private String passScenicspot;
    private String travelCost;
    private String travelDate;
    private String travelTime;
    private int praiseNum;
    private int collectNum;
    private int browseNum;
    private int  notesStatus;
    private int createBy;
    private java.sql.Timestamp createTime;
    // 游记发布者
    private LgNormalUser user;

}
