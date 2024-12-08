package ch.mvurdorf.platform;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jooq.ResultQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.name;
import static com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With.owner;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "ch.mvurdorf.platform", importOptions = DoNotIncludeTests.class)
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
}
