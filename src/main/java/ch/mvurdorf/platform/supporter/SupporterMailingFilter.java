package ch.mvurdorf.platform.supporter;

import ch.mvurdorf.platform.utils.DateUtil;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER;
import static ch.mvurdorf.platform.jooq.Tables.SUPPORTER_PAYMENT;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.EMAIL;
import static ch.mvurdorf.platform.supporter.SupporterMailingChannel.POST;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jooq.impl.DSL.selectOne;

/**
 * Selects the supporters a mailing is sent to.
 *
 * @param types            only supporters of these types (empty = all types)
 * @param paidWithinMonths only supporters with a payment in the last n months (null = no restriction)
 * @param email            whether supporters reachable by email are included
 * @param post             whether supporters reachable by post are included
 */
public record SupporterMailingFilter(Set<SupporterType> types,
                                     Integer paidWithinMonths,
                                     boolean email,
                                     boolean post) {

    public SupporterMailingFilter {
        types = types == null || types.isEmpty() ? Set.of() : Set.copyOf(types);
    }

    public static SupporterMailingFilter defaultFilter() {
        return new SupporterMailingFilter(Set.of(), null, true, true);
    }

    public Condition condition() {
        var condition = DSL.noCondition();
        if (!types.isEmpty()) {
            condition = condition.and(SUPPORTER.TYPE.in(types.stream().map(Enum::name).toList()));
        }
        if (paidWithinMonths != null) {
            condition = condition.and(DSL.exists(selectOne().from(SUPPORTER_PAYMENT)
                                                            .where(SUPPORTER_PAYMENT.FK_SUPPORTER.eq(SUPPORTER.ID))
                                                            .and(SUPPORTER_PAYMENT.DATUM.ge(DateUtil.today().minusMonths(paidWithinMonths)))));
        }
        return condition;
    }

    /**
     * A supporter gets an email if an email is available and they want communication by email, and a letter if no
     * email is available or they want communication by post (both is possible). Only the channels enabled in this
     * filter are returned; an empty result means the supporter is not a recipient.
     */
    public Set<SupporterMailingChannel> channels(SupporterDto supporter) {
        var channels = EnumSet.noneOf(SupporterMailingChannel.class);
        if (email && isNotBlank(supporter.email()) && supporter.kommunikationEmail()) {
            channels.add(EMAIL);
        }
        if (post && (isBlank(supporter.email()) || supporter.kommunikationPost())) {
            channels.add(POST);
        }
        return channels;
    }

    public String typesAsString() {
        return types.isEmpty() ? null : types.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }

    public static Set<SupporterType> typesFromString(String types) {
        if (isBlank(types)) {
            return Set.of();
        }
        return Arrays.stream(types.split(","))
                     .map(SupporterType::valueOf)
                     .collect(Collectors.toUnmodifiableSet());
    }
}
