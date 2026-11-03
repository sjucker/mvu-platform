package ch.mvurdorf.platform.supporter;

import java.time.LocalDateTime;

public record SupporterMailingDto(Long id,
                                  String subject,
                                  String mjml,
                                  SupporterMailingFilter filter,
                                  LocalDateTime createdAt,
                                  String createdBy,
                                  LocalDateTime updatedAt,
                                  String updatedBy,
                                  LocalDateTime sentAt,
                                  String sentBy,
                                  int emailCount,
                                  int postCount) {

    public boolean isSent() {
        return sentAt != null;
    }
}
