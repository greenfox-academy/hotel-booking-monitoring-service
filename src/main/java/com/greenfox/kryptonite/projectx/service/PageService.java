package com.greenfox.kryptonite.projectx.service;

import com.greenfox.kryptonite.projectx.model.pageviews.DataAttributes;
import com.greenfox.kryptonite.projectx.model.pageviews.EventToDatabase;
import com.greenfox.kryptonite.projectx.model.pageviews.NewPageViewFormat;
import com.greenfox.kryptonite.projectx.model.pageviews.PageViewData;
import com.greenfox.kryptonite.projectx.model.pageviews.PageViewLinks;
import com.greenfox.kryptonite.projectx.repository.EventToDatabaseRepository;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PageService {

  private final Integer ITEMS_PER_PAGE = 5;


  @Autowired
  EventToDatabaseRepository eventToDatabaseRepository;

  public NewPageViewFormat returnPage(HttpServletRequest request, Integer pageNumber,
      Integer min, Integer max, String path) {
    pageNumber = setPageNumber(pageNumber);
    List<EventToDatabase> requestedPageViews = createListOfFilteredPageViews(pageNumber, min, max,
        path);
    List<PageViewData> pageViewDataList = createPageViewDataList(requestedPageViews, pageNumber);
    PageViewLinks pageViewLinks = createLinks(pageNumber, request);
    return new NewPageViewFormat(pageViewLinks, pageViewDataList);
  }

  public int setPageNumber(Integer pageNumber) {
    if (pageNumber == null) {
      pageNumber = 0;
    } else {
      pageNumber = pageNumber - 1;
    }
    return pageNumber;
  }

  public List<EventToDatabase> createListOfFilteredPageViews(Integer pageNumber, Integer min,
      Integer max, String path) {
    if (min != null || max != null || path != null) {
      return new ArrayList<>(filterPageviews(min, max, path));
    } else {
      return new ArrayList<>(eventToDatabaseRepository
          .findAll(new PageRequest(pageNumber, ITEMS_PER_PAGE)).getContent());
    }
  }

  public PageViewLinks createLinks(Integer pageNumber, HttpServletRequest request) {
    String url = request.getRequestURL().toString();
    PageViewLinks pageViewLinks = new PageViewLinks();

    setSelf(pageViewLinks, request, url);

    if (request.getQueryString() == null || request.getQueryString().contains("page")) {
      Page page = eventToDatabaseRepository.findAll(new PageRequest(pageNumber, ITEMS_PER_PAGE));
      pageNumber++;
      setLast(pageViewLinks, page, url);
      setNext(pageViewLinks, page, pageNumber, url);
      setPrevious(pageViewLinks, page, pageNumber, url);
    }
    return pageViewLinks;
  }


  public List<PageViewData> createPageViewDataList(List<EventToDatabase> list, Integer pageNumber) {
    List<PageViewData> pageViewDataList = new ArrayList<>();
    long id = (pageNumber * ITEMS_PER_PAGE) + 1;
    for (EventToDatabase event : list) {
      pageViewDataList.add(new PageViewData(event.getType(), id,
          new DataAttributes(event.getPath(), event.getCount())));
      id++;
    }
    return pageViewDataList;
  }

  public void setSelf(PageViewLinks pageViewLinks, HttpServletRequest request, String url) {
    if (request.getQueryString() == null) {
      pageViewLinks.setSelf(url);
    } else {
      pageViewLinks.setSelf(url + "?" + request.getQueryString());
    }
  }

  public void setLast(PageViewLinks pageViewLinks, Page page, String url) {
    pageViewLinks.setLast(url + "?page=" + page.getTotalPages());
  }

  public void setNext(PageViewLinks pageViewLinks, Page page, Integer pageNumber, String url) {
    if (page.hasNext()) {
      pageViewLinks.setNext(url + "?page=" + (pageNumber + 1));
    } else {
      pageViewLinks.setLast("This is the last page");
    }
  }

  public void setPrevious(PageViewLinks pageViewLinks, Page page, Integer pageNumber, String url) {
    if (page.hasPrevious()) {
      pageViewLinks.setPrev(url + "?page=" + (pageNumber - 1));
    }
  }

  public List<EventToDatabase> filterPageviews(Integer min, Integer max, String path) {
    if (path != null) {
      return eventToDatabaseRepository.findAllByPath(path);
//    } else if (min != null || max != null) {
//      pageviews = filterPageviewsByCount(pageviews, min, max);
    } else {
      return eventToDatabaseRepository.findAllByOrderByIdAsc();
    }
  }

}
