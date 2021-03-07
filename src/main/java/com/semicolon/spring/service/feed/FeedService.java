package com.semicolon.spring.service.feed;

import com.semicolon.spring.dto.FeedDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedService {
    FeedDTO.messageResponse fileUpload(List<MultipartFile> files, int feedId);
    FeedDTO.writeFeedResponse writeFeed(FeedDTO.feed request, int club_id);
    List<FeedDTO.getFeed> getFeedList(int page);
    List<FeedDTO.getFeedClub> getFeedClubList(int page, int club_id);
    FeedDTO.messageResponse feedModify(FeedDTO.feed request, int feedId);
    FeedDTO.messageResponse feedFlag(int feedId);
    FeedDTO.getFeed getFeed(int feedId);
    FeedDTO.messageResponse deleteFeed(int feedId);
    FeedDTO.messageResponse feedPin(int feedId);
    List<FeedDTO.getFeed> getFeedList();
    List<FeedDTO.userResponse> getFeedUser(int feedId);
}
