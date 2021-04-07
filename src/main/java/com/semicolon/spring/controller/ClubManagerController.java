package com.semicolon.spring.controller;

import com.semicolon.spring.dto.ClubDTO;
import com.semicolon.spring.service.club_manager.ClubManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubManagerController {

    private final ClubManagerService clubHeadService;

    @PostMapping("/{club_id}/recruitment")
    public ClubDTO.messageResponse recruitment(@RequestBody @Valid ClubDTO.recruitment request, @PathVariable("club_id") int club_id) throws ExecutionException, InterruptedException {
        return clubHeadService.recruitment(request, club_id);
    }

    @PostMapping("/{club_id}/profile")
    public ClubDTO.messageResponse profile(@RequestParam("file") MultipartFile file, @PathVariable("club_id") int club_id){
        return clubHeadService.clubProfile(file, club_id);
    }

    @PostMapping("/{club_id}/hongbo")
    public ClubDTO.messageResponse hongbo(@RequestParam("file") MultipartFile file, @PathVariable("club_id") int club_id){
        return clubHeadService.clubHongbo(file, club_id);
    }

    @PostMapping("/{club_id}/banner")
    public ClubDTO.messageResponse banner(@RequestParam("file") MultipartFile file, @PathVariable("club_id") int club_id){
        return clubHeadService.clubBanner(file, club_id);
    }

    @PutMapping("/{club_id}")
    public ClubDTO.messageResponse modify(@RequestBody ClubDTO.modify request, @PathVariable("club_id") int club_id){
        return clubHeadService.modifyClub(request, club_id);
    }

    @PostMapping("/{club_id}/description")
    public ClubDTO.messageResponse description(@RequestBody ClubDTO.description request, @PathVariable("club_id") int club_id){
        return clubHeadService.clubDescription(request, club_id);
    }

    @DeleteMapping("/{club_id}/recruitment")
    public ClubDTO.messageResponse deleteRecruitment(@PathVariable("club_id") int club_id){
        return clubHeadService.deleteRecruitment(club_id);
    }

    @GetMapping("/{club_id}/hongbo")
    public ClubDTO.hongbo getHongbo(@PathVariable("club_id") int club_id){
        return clubHeadService.getHongbo(club_id);
    }

    @DeleteMapping("/{club_id}/hongbo")
    public ClubDTO.messageResponse deleteHongbo(@PathVariable("club_id") int club_id){
        return clubHeadService.deleteHongbo(club_id);
    }



}