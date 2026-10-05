package ch.mvurdorf.platform;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.DependencyRules;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.library.ProxyRules;
import org.jooq.ResultQuery;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "ch.mvurdorf.platform")
public class ArchUnitTest {

    // usage of jOOQ's stream is dangerous since it must be closed manually or in a try-with-resources
    // this is often forgotten, therefore, we prohibit it entirely
    @ArchTest
    public static final ArchRule no_jooq_stream = noClasses().should().callMethodWhere(target(name("stream"))
                                                                                               .and(target(owner(assignableTo(ResultQuery.class)))));

    // always use DateUtil
    @ArchTest
    public static final ArchRule no_now_without_zone = noClasses()
            .should().callMethod(LocalDate.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(LocalTime.class, "now");

    @ArchTest
    public static final ArchRule no_classes_should_access_standard_streams = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
    @ArchTest
    public static final ArchRule no_classes_should_throw_generic_exceptions = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
    @ArchTest
    public static final ArchRule no_classes_should_use_jodatime = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;
    @ArchTest
    public static final ArchRule no_classes_should_use_java_util_logging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
    @ArchTest
    public static final ArchRule no_classes_should_use_field_injection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
    @ArchTest
    public static final ArchRule testClassesShouldResideInTheSamePackageAsImplementation = GeneralCodingRules.testClassesShouldResideInTheSamePackageAsImplementation();
    @ArchTest
    public static final ArchRule old_date_and_time_classes_should_not_be_used = GeneralCodingRules.OLD_DATE_AND_TIME_CLASSES_SHOULD_NOT_BE_USED;
    @ArchTest
    public static final ArchRule assertions_should_have_detail_message = GeneralCodingRules.ASSERTIONS_SHOULD_HAVE_DETAIL_MESSAGE;
    @ArchTest
    public static final ArchRule deprecated_api_should_not_be_used = GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;

    // generated jOOQ code is excluded from the following rules
    private static final String JOOQ_PACKAGE = "ch.mvurdorf.platform.jooq..";

    @ArchTest
    public static final ArchRule no_classes_should_depend_upper_packages = noClasses().that().resideOutsideOfPackage(JOOQ_PACKAGE)
                                                                                      .should(DependencyRules.dependOnUpperPackages());

    // self-invocation bypasses the Spring proxy, so the annotation would have no effect
    @ArchTest
    public static final ArchRule no_self_invocation_of_transactional = noClasses().that().resideOutsideOfPackage(JOOQ_PACKAGE)
                                                                                  .should(ProxyRules.directly_call_other_methods_declared_in_the_same_class_that_are_annotated_with(Transactional.class));
    @ArchTest
    public static final ArchRule no_self_invocation_of_async = noClasses().that().resideOutsideOfPackage(JOOQ_PACKAGE)
                                                                          .should(ProxyRules.directly_call_other_methods_declared_in_the_same_class_that_are_annotated_with(Async.class));

}
