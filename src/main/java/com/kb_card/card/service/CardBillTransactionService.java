package com.kb_card.card.service;

import com.kb_card.card.entity.CardTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardBillTransactionService {

    private final CardBillService cardBillService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTransactionToBillSafely(CardTransaction transaction) {
        try {
            cardBillService.addTransactionToBill(transaction);
            log.info("청구서 반영 성공 - transactionId: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("청구서 반영 실패 - transactionId: {}, error: {}",
                    transaction.getTransactionId(), e.getMessage(), e);
        }
    }
}