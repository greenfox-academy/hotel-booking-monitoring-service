package com.greenfox.kryptonite.projectx.service;

import com.greenfox.kryptonite.projectx.model.pageviews.EventToDatabase;
import com.greenfox.kryptonite.projectx.repository.EventToDatabaseRepository;
import java.util.ArrayList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationService {

  final String HOST = "https://greenfox-kryptonite.herokuapp.com/pageviews?page=";


  ArrayList<EventToDatabase> pagination(EventToDatabaseRepository repo, int page) {
    ArrayList<EventToDatabase> finalList = new ArrayList<>();
    ArrayList<EventToDatabase> allEventList = (ArrayList<EventToDatabase>) repo.findAllByOrderByIdAsc();

    int endIndex = checkEndIndex(page, allEventList);

    if ((page != 0) && (allEventList.size() > 20)) {
      for (int i = page * 20 - 20; i < endIndex; ++i) {
        finalList.add(allEventList.get(i));
      }
    } else {
      finalList = allEventList;
    }
    return finalList;
  }

  int checkEndIndex(int page, ArrayList<EventToDatabase> allEventList) {
    int endIndex = 0;
    if (allEventList.size() < page * 20) {
      endIndex = allEventList.size();
    } else {
      endIndex = page * 20;
    }
    return endIndex;
  }

  String checkNextPage(String self, String last, int page) {
    String next;
    if (self.equals(last)) {
      next = "this is the last page";
    } else {
      next = HOST + (page + 1);
    }
    return next;
  }

  String checkPrevPage(int page) {
    return HOST + (page - 1);
  }
}