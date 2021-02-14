package com.semicolon.spring.service.feed;

import com.semicolon.spring.dto.FeedDTO;
import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.club.ClubRepository;
import com.semicolon.spring.entity.club.application.ApplicationRepository;
import com.semicolon.spring.entity.club.club_follow.ClubFollowRepository;
import com.semicolon.spring.entity.club.club_head.ClubHead;
import com.semicolon.spring.entity.club.club_head.ClubHeadRepository;
import com.semicolon.spring.entity.feed.Feed;
import com.semicolon.spring.entity.feed.FeedRepository;
import com.semicolon.spring.entity.feed.MediaComparator;
import com.semicolon.spring.entity.feed.feed_flag.FeedFlag;
import com.semicolon.spring.entity.feed.feed_flag.FeedFlagRepository;
import com.semicolon.spring.entity.feed.feed_medium.FeedMedium;
import com.semicolon.spring.entity.feed.feed_medium.FeedMediumRepository;
import com.semicolon.spring.entity.user.User;
import com.semicolon.spring.exception.*;
import com.semicolon.spring.security.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedServiceImpl implements FeedService{
    private final FeedRepository feedRepository;
    private final FeedMediumRepository feedMediumRepository;
    private final ClubRepository clubRepository;
    private final ApplicationRepository applicationRepository;
    private final FeedFlagRepository feedFlagRepository;
    private final ClubFollowRepository clubFollowRepository;
    private final ClubHeadRepository clubHeadRepository;
    private final AuthenticationFacade authenticationFacade;

    @Value("${file.path}")
    private String PATH;

    //Security Context에서 가져오는 User정보가 null이 아니라면 is follow와 isflag를 return한다. 만약 User정보가 null이라면 둘 다 false를 return한다.

    @Override
    public FeedDTO.messageResponse fileUpload(MultipartFile file, int feedId) { // feed가 자기 클럽이 쓴것인지 확인.
        if(isNotClubMember(feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new).getClub().getClubId()))
            throw new NotClubMemberException();
        try{
            Random random = new Random(System.currentTimeMillis());
            String fileString = random.nextInt() + file.getOriginalFilename();
            file.transferTo(new File(PATH + fileString));
            feedRepository.findById(feedId)
                    .map(feed-> feedMediumRepository.save(FeedMedium.builder()
                            .feed(feed)
                            .medium_path("feed/" + fileString)
                            .build())
                    );
            log.info("fileUpload feed_id : " + feedId);
            return new FeedDTO.messageResponse("File upload success.");
        }catch (IOException e){
            e.printStackTrace();
            throw new FileSaveFailException();
        }
    }

    @Override
    public FeedDTO.writeFeedResponse writeFeed(FeedDTO.feed request, int club_id) {
        if(isNotClubMember(club_id))
            throw new NotClubMemberException();
        if(request.isPin()&&!isClubHead(club_id)){
            throw new NoAuthorityException();
        }
        log.info("writeFeed club_id : " + club_id);
        return new FeedDTO.writeFeedResponse("feed writing success",
                feedRepository.save(
                    Feed.builder()
                        .contents(request.getContent())
                        .pin(request.isPin())
                        .club(clubRepository.findByClubId(club_id))
                        .build()
                ).getId());
    }

    @Override
    public List<FeedDTO.getFeed> getFeedList(int page) {
        return feedToResponse(getFeeds(page).getContent(), page);
    }

    @Override
    public List<FeedDTO.getFeedClub> getFeedClubList(int page, int club_id) {
        return feedClubToResponse(getFeedClub(page, club_id).getContent(), page);
    }

    @Override
    public FeedDTO.messageResponse feedModify(FeedDTO.feed request, int feedId) { // feed를 쓴 클럽인지 확인절차 추가.
        Club club = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new).getClub();
        if(isNotClubMember(club.getClubId()))
            throw new NotClubMemberException();
        if(request.isPin()&&!isClubHead(club.getClubId())){
            throw new NoAuthorityException();
        }
        if(feedRepository.findByClubAndPinIsTrue(club).size()>=1){
            throw new BadRequestException();
        }
        feedRepository.findById(feedId)
                .map(feed -> {
                    feed.modify(request.getContent(), request.isPin());
                    feedRepository.save(feed);
                    return feed;
                }).orElseThrow(FeedNotFoundException::new);
        log.info("feedModify feed_id : " + feedId);
        return new FeedDTO.messageResponse("feed writing success");
    }

    @Override
    public FeedDTO.messageResponse feedFlag(int feedId) {
        User user = authenticationFacade.getUser();
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
        if(isFlag(user, feed)){
            feedFlagRepository.delete(feedFlagRepository.findByUserAndFeed(user, feed).orElseThrow(BadRequestException::new));
            log.info("Remove Feed Flag user_id : " + user.getUser_id());
            return new FeedDTO.messageResponse("Remove Feed Flag Success");
        }else{
            feedFlagRepository.save(
                    FeedFlag.builder()
                    .user(user)
                    .feed(feed)
                    .build()
            );
            log.info("Add Feed Flag user_id : " + user.getUser_id());
            return new FeedDTO.messageResponse("Add Feed Flag Success");
        }

    }

    @Override
    public FeedDTO.getFeed getFeed(int feedId) {
        User user = authenticationFacade.getUser();
        return feedRepository.findById(feedId)
                .map(feed -> {
                    FeedDTO.getFeed getFeed = FeedDTO.getFeed.builder()
                            .feedId(feed.getId())
                            .clubName(feed.getClub().getClub_name())
                            .profileImage(feed.getClub().getProfile_image())
                            .content(feed.getContents())
                            .media(getMediaPath(feed.getMedia()))
                            .uploadAt(feed.getUploadAt())
                            .flags(feedFlagRepository.countByFeed(feed))
                            .build();
                    if(user!=null){
                        getFeed.setIsFlag(isFlag(user, feed));
                        getFeed.setIsFollow(clubFollowRepository.findByUserAndClub(user, feed.getClub()).isPresent());
                    }
                    log.info("getFeed feedId : " + feedId);
                    return getFeed;
                }).orElseThrow(FeedNotFoundException::new);
    }

    @Override
    public FeedDTO.messageResponse deleteFeed(int feedId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);
        if(isNotClubMember(feed.getClub().getClubId()))
            throw new NotClubMemberException();
        feedRepository.delete(feed);
        log.info("deleteFeed feedId : " + feedId);
        return new FeedDTO.messageResponse("Feed delete success.");
    }

    @Override
    public FeedDTO.messageResponse feedPin(int feedId) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(FeedNotFoundException::new);

        if(!isClubHead(feed.getClub().getClubId())){
            throw new NoAuthorityException();
        }
        if(feedRepository.findByClubAndPinIsTrue(feed.getClub()).size()>=1){
            throw new BadRequestException();
        }

        feed.changePin();
        feedRepository.save(feed);
        log.info("feed pin change success feedId : " + feedId);

        return new FeedDTO.messageResponse("feed pin change success");
    }

    private boolean isFlag(User user, Feed feed){
        if(user!=null)
            return feedFlagRepository.findByUserAndFeed(user, feed).isPresent();
        else throw new UserNotFoundException();
    }

    public List<FeedDTO.getFeed> feedToResponse(List<Feed> feeds, int page){ // 유저 정보가 있을 때 isFlag, isFollow
        List<FeedDTO.getFeed> response = new ArrayList<>();
        User user = authenticationFacade.getUser();
        for(Feed feed : feeds){
            FeedDTO.getFeed getFeed = FeedDTO.getFeed.builder()
                    .feedId(feed.getId())
                    .clubName(feed.getClub().getClub_name())
                    .profileImage(feed.getClub().getProfile_image())
                    .content(feed.getContents())
                    .media(getMediaPath(feed.getMedia()))
                    .uploadAt(feed.getUploadAt())
                    .flags(feedFlagRepository.countByFeed(feed))
                    .build();

            if(user!=null){
                getFeed.setIsFlag(isFlag(user, feed));
                getFeed.setIsFollow(clubFollowRepository.findByUserAndClub(user, feed.getClub()).isPresent());
                getFeed.setOwner(!isNotClubMember(feed.getClub().getClubId()));
            }
            response.add(getFeed);
        }
        log.info("get feedList page : " + page);
        return response;
    }

    public List<FeedDTO.getFeedClub> feedClubToResponse(List<Feed> feeds, int page){ // 유저 정보가 있을 때 isFlag, isFollow
        List<FeedDTO.getFeedClub> response = new ArrayList<>();
        User user = authenticationFacade.getUser();
        for(Feed feed : feeds){
            FeedDTO.getFeedClub getFeedClub = FeedDTO.getFeedClub.builder()
                    .feedId(feed.getId())
                    .clubName(feed.getClub().getClub_name())
                    .profileImage(feed.getClub().getProfile_image())
                    .content(feed.getContents())
                    .media(getMediaPath(feed.getMedia()))
                    .uploadAt(feed.getUploadAt())
                    .isPin(feed.isPin())
                    .flags(feedFlagRepository.countByFeed(feed))
                    .build();
            if(user!=null){
                getFeedClub.setIsFlag(isFlag(user, feed));
                getFeedClub.setIsFollow(clubFollowRepository.findByUserAndClub(user, feed.getClub()).isPresent());
                getFeedClub.setOwner(!isNotClubMember(feed.getClub().getClubId()));
            }
            response.add(getFeedClub);
        }
        log.info("get feedClubList page : " + page);
        return response;
    }

    public List<String> getMediaPath(Set<FeedMedium> feedMedia){
        List<FeedMedium> list = new ArrayList<>(feedMedia);
        List<String> response = new ArrayList<>();
        Collections.sort(list, new MediaComparator());
        for(FeedMedium feedMedium : list){
            response.add(feedMedium.getMedium_path());
        }
        return response;
    }

    public Page<Feed> getFeeds(int page){
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("uploadAt").descending());
        return feedRepository.findAll(pageRequest);
    }

    public Page<Feed> getFeedClub(int page, int club_id){
        Club club = clubRepository.findById(club_id).orElseThrow(ClubNotFoundException::new);
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("pin").descending().and(Sort.by("uploadAt").descending()));
        return feedRepository.findByClub(club, pageRequest);
    }

    private boolean isNotClubMember(int club_id){ // user가 속해있지 않은 club_id를 보내는 테스트 해야함.
        User user = authenticationFacade.getUser();
        Club club = clubRepository.findByClubId(club_id);
        if(user == null)
            throw new UserNotFoundException();
        if(club == null)
            throw new ClubNotFoundException();
        return applicationRepository.findByUserAndClub(user, club) == null && !user.getHead().contains(club.getClubHead());
    }

    private boolean isClubHead(int club_id){
        User user = authenticationFacade.getUser();
        Club club = clubRepository.findById(club_id).orElseThrow(ClubNotFoundException::new);
        ClubHead clubHead = clubHeadRepository.findByClubAndUser(club, user);
        if(clubHead == null)
            throw new NotClubHeadException();
        else return true;
    }
}
