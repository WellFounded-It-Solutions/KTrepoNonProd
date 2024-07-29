package se.infomaker.frtutilities

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(DateUtilEnglishTest::class, DateUtilNorwegianTest::class, DateUtilSwedishTest::class)
class DateUtilTestSuite