package com.eliezercruz.ledxcalc.domain

object BasesDistribution {
    fun baseLines(support: SupportCalculation): List<String> = buildList {
        if (support.base4 > 0) add("Bases de piso de 4: ${support.base4}")
        if (support.base3 > 0) add("Bases de piso de 3: ${support.base3}")
        if (support.base2 > 0) add("Bases de piso de 2: ${support.base2}")
    }

    fun stairLines(support: SupportCalculation): List<String> = buildList {
        if (support.stair4 > 0) add("Escaleras de 4: ${support.stair4}")
        if (support.stair3 > 0) add("Escaleras de 3: ${support.stair3}")
        if (support.stair2 > 0) add("Escaleras de 2: ${support.stair2}")
    }
}
