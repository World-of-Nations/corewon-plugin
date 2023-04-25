package fr.world.nations.koth.models;

/*
 *  * @Created on 18/08/2022
 *  * @Project KOTH-WON
 *  * @Author Jimmy  / SKAH#7513
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class PowerAddedModel {


    private final String factionTagId;

    private long mustRemoveAt;

    private double power;

}
