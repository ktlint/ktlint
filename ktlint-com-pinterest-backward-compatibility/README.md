= Purpose

The purpose of this module is to provide backwards compatibility of rulesets created with Ktlint 1.3.x - 1.8.x with the Ktlint 2.x rule engine so that older rulesets can still be loaded with Ktlint CLI, Ktlint IntelliJ Plugin, and other Ktlint integrators for which this is relevant.

Classes provided in packages with starting with "com.pinterest.ktlint" should not be used in rulesets that are migrated to Ktlint 2.x. By extracting those classes to a separate Gradle module, migrating a ruleset to Ktlint 2.x should become more convenient as not duplicate class hierarchies exist as long as this module is not added as dependency.

This module will be removed in a later version of Ktlint when users of all Ktlint integrators have had sufficient time to upgrade to Ktlint rules that are compatible with Ktlint 2.x.
