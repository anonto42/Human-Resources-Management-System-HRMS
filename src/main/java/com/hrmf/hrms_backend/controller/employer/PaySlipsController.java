package com.hrmf.hrms_backend.controller.employer;

import com.hrmf.hrms_backend.dto.paySlip.CreatePaySlipe;
import com.hrmf.hrms_backend.dto.paySlip.PaginationResponse;
import com.hrmf.hrms_backend.dto.paySlip.ResponsePaySlipe;
import com.hrmf.hrms_backend.dto.paySlip.UpdatePaySlipe;
import com.hrmf.hrms_backend.service.PaySlipsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employer/pay-slips")
@RequiredArgsConstructor
public class PaySlipsController {

    private final PaySlipsService paySlipsService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPaySlipe(
            @Valid @ModelAttribute CreatePaySlipe paySlipe
    ) {
        ResponsePaySlipe response = paySlipsService.createPaySlip(paySlipe);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginationResponse<ResponsePaySlipe>> getPaySlipsPaginated(
            @RequestParam(required = false) String monthYear,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PaginationResponse<ResponsePaySlipe> response = paySlipsService.getPaySlipsByEmployer(
                monthYear, department, search, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsePaySlipe> getPaySlipById(@PathVariable String id) {
        ResponsePaySlipe response = paySlipsService.getPaySlipById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponsePaySlipe> updatePaySlip(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdatePaySlipe paySlipe
    ) {
        ResponsePaySlipe response = paySlipsService.updatePaySlip(id, paySlipe);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaySlip(@PathVariable UUID id) {
        paySlipsService.deletePaySlip(id);
        return ResponseEntity.noContent().build();
    }
}