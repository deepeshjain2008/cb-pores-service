package com.igot.cb.org;

import com.igot.cb.org.service.OrgService;
import com.igot.cb.pores.util.ApiResponse;
import com.igot.cb.pores.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/org")
@Slf4j
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @GetMapping("/framework/read")
    public ResponseEntity<Object> readFramework(@RequestParam String frameworkName, @RequestParam String orgId, @RequestParam String termName, @RequestHeader(value = Constants.X_AUTH_TOKEN) String userAuthToken) {
        ApiResponse response = orgService.readFramework(frameworkName,orgId,termName,userAuthToken);
        return new ResponseEntity<>(response,response.getResponseCode());
    }

    @PostMapping("/hierarchy/framework")
    public ResponseEntity<Object> createFramework(@RequestParam String masterFrameworkName, @RequestParam String orgId, @RequestHeader(value = Constants.X_AUTH_TOKEN) String userAuthToken) {
        ApiResponse response = orgService.createOrgHierarchyFramework(masterFrameworkName,orgId,userAuthToken);
        return new ResponseEntity<>(response,response.getResponseCode());
    }

}
