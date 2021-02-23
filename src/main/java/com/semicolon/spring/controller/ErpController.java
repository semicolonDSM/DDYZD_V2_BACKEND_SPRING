package com.semicolon.spring.controller;

import com.semicolon.spring.dto.ErpDTO;
import com.semicolon.spring.service.erp.ErpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ErpController {

    private final ErpService erpService;

    @GetMapping("/club/{club_id}/supply")
    public ErpDTO.Supply supply(@PathVariable("club_id") int club_id, @RequestBody ErpDTO.Url url) throws IOException {
        return erpService.supply(url);
    }

}
