package com.semicolon.spring.service.application;

import com.semicolon.spring.dto.HeadDTO.*;
import com.semicolon.spring.entity.club.Club;
import com.semicolon.spring.entity.club.ClubRepository;
import com.semicolon.spring.entity.club.club_head.ClubHeadRepository;
import com.semicolon.spring.entity.club.room.Room;
import com.semicolon.spring.entity.club.room.RoomRepository;
import com.semicolon.spring.entity.club.room.RoomStatus;
import com.semicolon.spring.entity.user.User;
import com.semicolon.spring.entity.user.UserRepository;
import com.semicolon.spring.exception.*;
import com.semicolon.spring.security.AuthenticationFacade;
import com.semicolon.spring.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final AuthenticationFacade authenticationFacade;
    private final FcmService fcmService;
    private final ClubRepository clubRepository;
    private final ClubHeadRepository clubHeadRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;


    @Override
    public MessageResponse cancelApplication(int clubId) {
        var user = getUser();
        var club = clubRepository.findById(clubId).orElseThrow(ClubNotFoundException::new);

        deleteApplication(user, club);

        return new MessageResponse("Application Cancel Success");
    }

    @Override
    public MessageResponse removeApplication(int clubId, int userId) {
        if(isNotClubHead(clubId)){
            throw new NotClubHeadException();
        }

        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        var club = clubRepository.findById(clubId).orElseThrow(ClubNotFoundException::new);

        deleteApplication(user, club);

        return new MessageResponse("Application Remove Success");
    }

    private void deleteApplication(User user, Club club) {
        var application = roomRepository.findByUserAndClub(user, club);

        if(application == null)
            throw new ApplicationNotFoundException();

        if(club.getStartAt() != null){
            roomRepository.save(Room.builder()
                    .club(application.getClub())
                    .clubLooked(application.isClubLooked())
                    .userLooked(application.isUserLooked())
                    .status(RoomStatus.N)
                    .user(application.getUser())
                    .id(application.getId())
                    .build()
            );
        }else {
            roomRepository.save(Room.builder()
                    .club(application.getClub())
                    .clubLooked(application.isClubLooked())
                    .userLooked(application.isUserLooked())
                    .status(RoomStatus.C)
                    .user(application.getUser())
                    .id(application.getId())
                    .build()
            );
        }



        if(user.getDeviceToken()!=null){
            FcmRequest request = FcmRequest.builder()
                    .token(user.getDeviceToken())
                    .title(club.getName())
                    .message(user.getName() + "님의 " + club.getName() + "동아리 신청이 취소되었습니다.")
                    .club(club.getClubId())
                    .build();
            fcmService.send(request);
        }
    }


    private User getUser(){
        return authenticationFacade.getUser();
    }

    private boolean isNotClubHead(int clubId){
        var user = authenticationFacade.getUser();
        var club = clubRepository.findById(clubId).orElseThrow(ClubNotFoundException::new);
        clubHeadRepository.findByClubAndUser(club, user).orElseThrow(NotClubHeadException::new);
        return false;
    }

}
