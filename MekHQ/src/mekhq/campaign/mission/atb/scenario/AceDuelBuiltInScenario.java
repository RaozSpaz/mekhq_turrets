/*
 * Copyright (c) 2019-2022 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.mission.atb.scenario;

import java.util.ArrayList;

import megamek.common.Compute;
import megamek.common.Entity;
import megamek.common.EntityWeightClass;
import megamek.common.UnitType;
import megamek.common.planetaryconditions.Fog;
import megamek.common.planetaryconditions.Light;
import megamek.common.planetaryconditions.Weather;
import megamek.common.planetaryconditions.Wind;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.AtBScenario;
import mekhq.campaign.mission.CommonObjectiveFactory;
import mekhq.campaign.mission.ScenarioObjective;
import mekhq.campaign.mission.atb.AtBScenarioEnabled;
import mekhq.campaign.unit.Unit;

@AtBScenarioEnabled
public class AceDuelBuiltInScenario extends AtBScenario {
    @Override
    public boolean isSpecialScenario() {
        return true;
    }

    @Override
    public int getScenarioType() {
        return ACEDUEL;
    }

    @Override
    public String getScenarioTypeDescription() {
        return "Special Scenario: Ace Duel";
    }

    @Override
    public String getResourceKey() {
        return "aceDuel";
    }

    @Override
    public void setLightConditions() {
        setLight(Light.DAY);
    }

    @Override
    public void setWeather() {
        setWeather(Weather.CLEAR);
        setWind(Wind.CALM);
        setFog(Fog.FOG_NONE);
    }

    @Override
    public void setMapFile() {
        setMap("Savannah");
        setTerrainType("Savannah");
    }

    @Override
    public boolean canRerollMap() {
        return false;
    }

    @Override
    public boolean canRerollLight() {
        return false;
    }

    @Override
    public boolean canRerollWeather() {
        return false;
    }

    @Override
    public boolean canDeploy(Unit unit, Campaign campaign) {
        return !unit.getCommander().getRank().isOfficer();
    }

    @Override
    public void setExtraScenarioForces(Campaign campaign, ArrayList<Entity> allyEntities, ArrayList<Entity> enemyEntities) {
        setStart(startPos[Compute.randomInt(4)]);
        int enemyStart = getStart() + 4;

        if (enemyStart > 8) {
            enemyStart -= 8;
        }

        for (int weight = EntityWeightClass.WEIGHT_ULTRA_LIGHT; weight <= EntityWeightClass.WEIGHT_COLOSSAL; weight++) {
            final Entity en;
            if (weight == EntityWeightClass.WEIGHT_COLOSSAL) {
                // Treat Colossal as a unique case, generating at that tier
                en = getEntity(getContract(campaign).getEnemyCode(), getContract(campaign).getEnemySkill(),
                        getContract(campaign).getEnemyQuality(), UnitType.MEK,
                        EntityWeightClass.WEIGHT_COLOSSAL, campaign);
            } else {
                // Generate up to a maximum of Assault
                en = getEntity(getContract(campaign).getEnemyCode(), getContract(campaign).getEnemySkill(),
                        getContract(campaign).getEnemyQuality(), UnitType.MEK,
                        Math.min(weight + 1, EntityWeightClass.WEIGHT_ASSAULT), campaign);
            }

            if (en == null) {
                getSpecialScenarioEnemies().add(new ArrayList<>());
                continue;
            }

            if (weight >= EntityWeightClass.WEIGHT_ASSAULT) {
                en.getCrew().setGunnery(en.getCrew().getGunnery() - 1);
                en.getCrew().setPiloting(en.getCrew().getPiloting() - 1);
            }

            enemyEntities = new ArrayList<>();
            enemyEntities.add(en);
            getSpecialScenarioEnemies().add(enemyEntities);
        }

        addBotForce(getEnemyBotForce(getContract(campaign), enemyStart, getSpecialScenarioEnemies().get(0)), campaign);
    }

    @Override
    public void setObjectives(Campaign campaign, AtBContract contract) {
        super.setObjectives(campaign, contract);

        ScenarioObjective destroyHostiles = CommonObjectiveFactory.getDestroyEnemies(contract, 1, 100);
        ScenarioObjective keepFriendliesAlive = CommonObjectiveFactory.getKeepFriendliesAlive(campaign, contract, this, 1, 100, false);

        getScenarioObjectives().add(destroyHostiles);
        getScenarioObjectives().add(keepFriendliesAlive);
    }
}
