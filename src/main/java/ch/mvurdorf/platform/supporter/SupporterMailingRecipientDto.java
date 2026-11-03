package ch.mvurdorf.platform.supporter;

import java.time.LocalDateTime;

public record SupporterMailingRecipientDto(Long supporterId,
                                           String name,
                                           String email,
                                           SupporterMailingChannel channel,
                                           LocalDateTime sentAt,
                                           String error) {
}
