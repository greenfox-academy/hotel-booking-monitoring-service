package com.greenfox.kryptonite.projectx.model.pageviews;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PageViewData {

  long id;
  String type;
  DataAttributes attributes;

  public PageViewData(String type, DataAttributes attributes) {
    this.type = type;
    this.attributes = attributes;
  }
}
