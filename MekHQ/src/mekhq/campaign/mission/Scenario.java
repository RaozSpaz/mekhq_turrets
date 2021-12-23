/*
 * Scenario.java
 *
 * Copyright (C) 2011-2016 - The MegaMek Team. All Rights Reserved.
 * Copyright (c) 2011 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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
package mekhq.campaign.mission;

import megamek.Version;
import megamek.client.ui.swing.lobby.LobbyUtility;
import megamek.common.*;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.DeploymentChangedEvent;
import mekhq.campaign.force.Force;
import mekhq.campaign.force.ForceStub;
import mekhq.campaign.mission.atb.AtBScenarioFactory;
import mekhq.campaign.mission.atb.IAtBScenario;
import mekhq.campaign.mission.enums.ScenarioStatus;
import mekhq.campaign.unit.Unit;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Scenario implements Serializable {
    //region Variable Declarations
    private static final long serialVersionUID = -2193761569359938090L;

    public static final int S_DEFAULT_ID = -1;

    /** terrain types **/
    public static final int TER_LOW_ATMO = -2;
    public static final int TER_SPACE = -1;
    public static final int TER_HILLS = 0;
    public static final int TER_BADLANDS = 1;
    public static final int TER_WETLANDS = 2;
    public static final int TER_LIGHTURBAN = 3;
    public static final int TER_FLATLANDS = 4;
    public static final int TER_WOODED = 5;
    public static final int TER_HEAVYURBAN = 6;
    public static final int TER_COASTAL = 7;
    public static final int TER_MOUNTAINS = 8;
    public static final String[] terrainTypes = {"Hills", "Badlands", "Wetlands",
            "Light Urban", "Flatlands", "Wooded", "Heavy Urban", "Coastal",
            "Mountains"
    };


    public static final int[] terrainChart = {
            TER_HILLS, TER_BADLANDS, TER_WETLANDS, TER_LIGHTURBAN,
            TER_HILLS, TER_FLATLANDS, TER_WOODED, TER_HEAVYURBAN,
            TER_COASTAL, TER_WOODED, TER_MOUNTAINS
    };

    private String name;
    private String desc;
    private String report;
    private ScenarioStatus status;
    private LocalDate date;
    private List<Integer> subForceIds;
    private List<UUID> unitIds;
    private int id = S_DEFAULT_ID;
    private int missionId;
    private ForceStub stub;
    private boolean cloaked;

    //allow multiple loot objects for meeting different mission objectives
    private List<Loot> loots;

    private List<ScenarioObjective> scenarioObjectives;

    /** Lists of enemy forces **/
    private List<BotForce> botForces;
    private List<BotForceStub> botForceStubs;

    // stores external id of bot forces
    private Map<String, Entity> externalIDLookup;

    /** map generation variables **/
    private int terrainType;
    private int mapSizeX;
    private int mapSizeY;
    private String map;
    private boolean usingFixedMap;

    /** planetary conditions parameters **/
    private int light;
    private int weather;
    private int wind;
    private int fog;
    private int atmosphere;
    private int temperature;
    private float gravity;
    private boolean emi;
    private boolean blowingSand;

    /** player starting position **/
    private int start;

    //Stores combinations of units and the transports they are assigned to
    private Map<UUID, List<UUID>> playerTransportLinkages;
    //endregion Variable Declarations

    public Scenario() {
        this(null);
    }

    public Scenario(String n) {
        this.name = n;
        desc = "";
        report = "";
        setStatus(ScenarioStatus.CURRENT);
        date = null;
        subForceIds = new ArrayList<>();
        unitIds = new ArrayList<>();
        loots = new ArrayList<>();
        scenarioObjectives = new ArrayList<>();
        playerTransportLinkages = new HashMap<>();
        botForces = new ArrayList<>();
        botForceStubs = new ArrayList<>();
        externalIDLookup = new HashMap<>();

        light = PlanetaryConditions.L_DAY;
        weather = PlanetaryConditions.WE_NONE;
        wind = PlanetaryConditions.WI_NONE;
        fog = PlanetaryConditions.FOG_NONE;
        atmosphere = PlanetaryConditions.ATMO_STANDARD;
        temperature = 25;
        gravity = (float) 1.0;
        emi = false;
        blowingSand = false;

    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getDescription() {
        return desc;
    }

    public void setDesc(String d) {
        this.desc = d;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String r) {
        this.report = r;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(final ScenarioStatus status) {
        this.status = status;
    }

    public @Nullable LocalDate getDate() {
        return date;
    }

    public void setDate(final @Nullable LocalDate date) {
        this.date = date;
    }

    public boolean hasObjectives() {
        return scenarioObjectives != null &&
                scenarioObjectives.size() > 0;
    }

    public List<ScenarioObjective> getScenarioObjectives() {
        return scenarioObjectives;
    }

    public void setScenarioObjectives(List<ScenarioObjective> scenarioObjectives) {
        this.scenarioObjectives = scenarioObjectives;
    }

    /**
     * This indicates that the scenario should not be displayed in the briefing tab.
     */
    public boolean isCloaked() {
        return cloaked;
    }

    public void setCloaked(boolean cloaked) {
        this.cloaked = cloaked;
    }

    public int getTerrainType() {
        return terrainType;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setTerrainType(int terrainType) {
        this.terrainType = terrainType;
    }

    public int getMapSizeX() {
        return mapSizeX;
    }

    public void setMapSizeX(int mapSizeX) {
        this.mapSizeX = mapSizeX;
    }

    public int getMapSizeY() {
        return mapSizeY;
    }

    public void setMapSizeY(int mapSizeY) {
        this.mapSizeY = mapSizeY;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getMapForDisplay() {
        if (!isUsingFixedMap()) {
            return getMap();
        } else {
            MapSettings ms = MapSettings.getInstance();
            return LobbyUtility.cleanBoardName(getMap(), ms);
        }
    }

    public boolean isUsingFixedMap() {
        return usingFixedMap;
    }

    public void setUsingFixedMap(boolean usingFixedMap) {
        this.usingFixedMap = usingFixedMap;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public int getWind() {
        return wind;
    }

    public void setWind(int wind) {
        this.wind = wind;
    }

    public int getFog() {
        return fog;
    }

    public void setFog(int fog) {
        this.fog = fog;
    }

    public int getAtmosphere() {
        return atmosphere;
    }

    public void setAtmosphere(int atmosphere) {
        this.atmosphere = atmosphere;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public boolean getEMI() {
        return emi;
    }

    public void setEMI(boolean emi) {
        this.emi = emi;
    }

    public boolean getBlowingSand() {
        return blowingSand;
    }

    public void setBlowingSand(boolean blow) {
        this.blowingSand = blow;
    }

    public Map<UUID, List<UUID>> getPlayerTransportLinkages() {
        return playerTransportLinkages;
    }

    /**
     * Adds a transport-cargo pair to the internal transport relationship store.
     * @param transportId the UUID of the transport object
     * @param cargoId     the UUID of the cargo being transported
     */
    public void addPlayerTransportRelationship(UUID transportId, UUID cargoId) {
        playerTransportLinkages.get(transportId).add(cargoId);
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int i) {
        this.missionId = i;
    }

    public List<Integer> getForceIDs() {
        return subForceIds;
    }

    public Force getForces(Campaign campaign) {
        Force force = new Force("Assigned Forces");
        for (int subid : subForceIds) {
            Force sub = campaign.getForce(subid);
            if (null != sub) {
                force.addSubForce(sub, false);
            }
        }
        for (UUID uid : unitIds) {
            force.addUnit(uid);
        }
        return force;
    }

    /**
     * Gets the IDs of units deployed to this scenario individually.
     */
    public List<UUID> getIndividualUnitIDs() {
        return unitIds;
    }

    public void addForces(int fid) {
        subForceIds.add(fid);
    }

    public void addUnit(UUID uid) {
        unitIds.add(uid);
    }

    public boolean containsPlayerUnit(UUID uid) {
        return unitIds.contains(uid);
    }

    public void removeUnit(UUID uid) {
        int idx = -1;
        for (int i = 0; i < unitIds.size(); i++) {
            if (uid.equals(unitIds.get(i))) {
                idx = i;
                break;
            }
        }
        if (idx > -1) {
            unitIds.remove(idx);
        }
    }

    public void removeForce(int fid) {
        List<Integer> toRemove = new ArrayList<>();
        for (Integer subForceId : subForceIds) {
            if (fid == subForceId) {
                toRemove.add(subForceId);
            }
        }
        subForceIds.removeAll(toRemove);
    }

    public void clearAllForcesAndPersonnel(Campaign campaign) {
        for (int fid : subForceIds) {
            Force f = campaign.getForce(fid);
            if (null != f) {
                f.clearScenarioIds(campaign);
                MekHQ.triggerEvent(new DeploymentChangedEvent(f, this));
            }
        }
        for (UUID uid : unitIds) {
            Unit u = campaign.getUnit(uid);
            if (null != u) {
                u.undeploy();
                MekHQ.triggerEvent(new DeploymentChangedEvent(u, this));
            }
        }
        subForceIds = new ArrayList<>();
        unitIds = new ArrayList<>();
    }

    /**
     * Converts this scenario to a stub
     */
    public void convertToStub(final Campaign campaign, final ScenarioStatus status) {
        setStatus(status);
        clearAllForcesAndPersonnel(campaign);
        generateStub(campaign);
    }

    public void generateStub(Campaign c) {
        stub = new ForceStub(getForces(c), c);
        for (BotForce bf : botForces) {
            botForceStubs.add(generateBotStub(bf));
        }
        botForces.clear();
    }

    public ForceStub getForceStub() {
        return stub;
    }

    public BotForceStub generateBotStub(BotForce bf) {
        return new BotForceStub("<html>" +
                bf.getName() + " <i>" +
                ((bf.getTeam() == 1) ? "Allied" : "Enemy") + "</i>" +
                " Start: " + IStartingPositions.START_LOCATION_NAMES[bf.getStart()] +
                " BV: " + bf.getTotalBV() +
                "</html>",
                generateEntityStub(bf.getEntityList()));
    }

    public List<String> generateEntityStub(List<Entity> entities) {
        List<String> stub = new ArrayList<>();
        for (Entity en : entities) {
            if (null == en) {
                stub.add("<html><font color='red'>No random assignment table found for faction</font></html>");
            } else {
                stub.add("<html>" + en.getCrew().getName() + " (" +
                        en.getCrew().getGunnery() + "/" +
                        en.getCrew().getPiloting() + "), " +
                        "<i>" + en.getShortName() + "</i>" +
                        "</html>");
            }
        }
        return stub;
    }

    public boolean isAssigned(Unit unit, Campaign campaign) {
        for (UUID uid : getForces(campaign).getAllUnits(true)) {
            if (uid.equals(unit.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<BotForce> getBotForces() {
        return botForces;
    }

    public void addBotForce(BotForce botForce) {
        botForces.add(botForce);

        // put all bot units into the external ID lookup.
        for (Entity entity : botForce.getEntityList()) {
            getExternalIDLookup().put(entity.getExternalIdAsString(), entity);
        }
    }

    public BotForce getBotForce(int i) {
        return botForces.get(i);
    }

    public void removeBotForce(int i) {
        botForces.remove(i);
    }

    public int getNumBots() {
        return getStatus().isCurrent() ? botForces.size() : botForceStubs.size();
    }

    public List<BotForceStub> getBotForceStubs() {
        return botForceStubs;
    }

    public Map<String, Entity> getExternalIDLookup() {
        return externalIDLookup;
    }

    public void setExternalIDLookup(HashMap<String, Entity> externalIDLookup) {
        this.externalIDLookup = externalIDLookup;
    }

    public void writeToXml(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent);
        writeToXmlEnd(pw1, indent);
    }

    protected void writeToXmlBegin(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "<scenario id=\""
                +id
                +"\" type=\""
                +this.getClass().getName()
                +"\">");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<name>"
                +MekHqXmlUtil.escape(getName())
                +"</name>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<desc>"
                +MekHqXmlUtil.escape(desc)
                +"</desc>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<report>"
                +MekHqXmlUtil.escape(report)
                +"</report>");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "start", start);
        MekHqXmlUtil.writeSimpleXMLTag(pw1, indent + 1, "status", getStatus().name());
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<id>"
                +id
                +"</id>");
        if (null != stub) {
            stub.writeToXml(pw1, indent+1);
        } else {
            // only bother writing out objectives for active scenarios
            if (hasObjectives()) {
                for (ScenarioObjective objective : this.scenarioObjectives) {
                    objective.Serialize(pw1);
                }
            }
        }
        if(!botForces.isEmpty() && getStatus().isCurrent()) {
            for (BotForce botForce : botForces) {
                pw1.println(MekHqXmlUtil.indentStr(indent + 1) + "<botForce>");
                botForce.writeToXml(pw1, indent + 1);
                pw1.println(MekHqXmlUtil.indentStr(indent + 1) + "</botForce>");
            }
        }
        if(!botForceStubs.isEmpty()) {
            for (BotForceStub botStub : botForceStubs) {
                pw1.println(MekHqXmlUtil.indentStr(indent + 1)
                        + "<botForceStub name=\""
                        + MekHqXmlUtil.escape(botStub.getName()) + "\">");
                for (String entity : botStub.getEntityList()) {
                    MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 2,
                            "entityStub", MekHqXmlUtil.escape(entity));
                }
                pw1.println(MekHqXmlUtil.indentStr(indent + 1)
                        + "</botForceStub>");
            }
        }
        if ((loots.size() > 0) && getStatus().isCurrent()) {
            pw1.println(MekHqXmlUtil.indentStr(indent+1)+"<loots>");
            for (Loot l : loots) {
                l.writeToXml(pw1, indent+2);
            }
            pw1.println(MekHqXmlUtil.indentStr(indent+1)+"</loots>");
        }
        if (null != date) {
            MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "date", MekHqXmlUtil.saveFormattedDate(date));
        }

        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent + 1, "cloaked", isCloaked());
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "terrainType", terrainType);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "usingFixedMap", isUsingFixedMap());
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<mapSize>"
                + mapSizeX + "," + mapSizeY
                +"</mapSize>");
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "map", map);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "light", light);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "weather", weather);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "wind", wind);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "fog", fog);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "temperature", temperature);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "atmosphere", atmosphere);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "gravity", gravity);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "emi", emi);
        MekHqXmlUtil.writeSimpleXmlTag(pw1, indent+1, "blowingSand", blowingSand);

    }

    protected void writeToXmlEnd(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</scenario>");
    }

    protected void loadFieldsFromXmlNode(final Node wn, final Version version, final Campaign campaign)
            throws ParseException {
        // Do nothing
    }

    public static Scenario generateInstanceFromXML(Node wn, Campaign c, Version version) {
        Scenario retVal = null;
        NamedNodeMap attrs = wn.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            // Instantiate the correct child class, and call its parsing function.
            if (className.equals(AtBScenario.class.getName())) {
                //Backwards compatibility when AtBScenarios were all part of the same class
                //Find the battle type and then load it through the AtBScenarioFactory

                NodeList nl = wn.getChildNodes();
                int battleType = -1;

                for (int x = 0; x < nl.getLength(); x++) {
                    Node wn2 = nl.item(x);

                    if (wn2.getNodeName().equalsIgnoreCase("battleType")) {
                        battleType = Integer.parseInt(wn2.getTextContent());
                        break;
                    }
                }

                if (battleType == -1) {
                    LogManager.getLogger().error("Unable to load an old AtBScenario because we could not determine the battle type");
                    return null;
                }

                List<Class<IAtBScenario>> scenarioClassList = AtBScenarioFactory.getScenarios(battleType);

                if ((null == scenarioClassList) || scenarioClassList.isEmpty()) {
                    LogManager.getLogger().error("Unable to load an old AtBScenario of battle type " + battleType);
                    return null;
                }

                retVal = (Scenario) scenarioClassList.get(0).newInstance();
            } else {
                retVal = (Scenario) Class.forName(className).newInstance();
            }

            retVal.loadFieldsFromXmlNode(wn, version, c);
            retVal.scenarioObjectives = new ArrayList<>();

            // Okay, now load Part-specific fields!
            NodeList nl = wn.getChildNodes();

            for (int x = 0; x < nl.getLength(); x++) {
                Node wn2 = nl.item(x);

                if (wn2.getNodeName().equalsIgnoreCase("name")) {
                    retVal.setName(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("status")) {
                    retVal.setStatus(ScenarioStatus.parseFromString(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("id")) {
                    retVal.id = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("desc")) {
                    retVal.setDesc(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("report")) {
                    retVal.setReport(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("forceStub")) {
                    retVal.stub = ForceStub.generateInstanceFromXML(wn2);
                } else if (wn2.getNodeName().equalsIgnoreCase("date")) {
                    retVal.date = MekHqXmlUtil.parseDate(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("cloaked")) {
                    retVal.cloaked = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("loots")) {
                    NodeList nl2 = wn2.getChildNodes();
                    for (int y = 0; y < nl2.getLength(); y++) {
                        Node wn3 = nl2.item(y);
                        // If it's not an element node, we ignore it.
                        if (wn3.getNodeType() != Node.ELEMENT_NODE)
                            continue;

                        if (!wn3.getNodeName().equalsIgnoreCase("loot")) {
                            // Error condition of sorts!
                            // Errr, what should we do here?
                            LogManager.getLogger().error("Unknown node type not loaded in techUnitIds nodes: " + wn3.getNodeName());
                            continue;
                        }
                        Loot loot = Loot.generateInstanceFromXML(wn3, c, version);
                        retVal.loots.add(loot);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase(ScenarioObjective.ROOT_XML_ELEMENT_NAME)) {
                    retVal.getScenarioObjectives().add(ScenarioObjective.Deserialize(wn2));
                } else if (wn2.getNodeName().equalsIgnoreCase("botForceStub")) {
                    String name = MekHqXmlUtil.unEscape(wn2.getAttributes().getNamedItem("name").getTextContent());
                    List<String> stub = getEntityStub(wn2);
                    retVal.botForceStubs.add(new BotForceStub(name, stub));
                }  else if (wn2.getNodeName().equalsIgnoreCase("botForce")) {
                    BotForce bf = new BotForce();
                    try {
                        bf.setFieldsFromXmlNode(wn2, version, c);
                    } catch (Exception e) {
                        LogManager.getLogger().error("Error loading bot force in scenario", e);
                        bf = null;
                    }

                    if (bf != null) {
                        retVal.addBotForce(bf);
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("usingFixedMap")) {
                    retVal.setUsingFixedMap(Boolean.parseBoolean(wn2.getTextContent().trim()));
                } else if (wn2.getNodeName().equalsIgnoreCase("terrainType")) {
                    retVal.terrainType = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("mapSize")) {
                    String []xy = wn2.getTextContent().split(",");
                    retVal.mapSizeX = Integer.parseInt(xy[0]);
                    retVal.mapSizeY = Integer.parseInt(xy[1]);
                } else if (wn2.getNodeName().equalsIgnoreCase("map")) {
                    retVal.map = wn2.getTextContent().trim();
                }  else if (wn2.getNodeName().equalsIgnoreCase("start")) {
                    retVal.start = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("light")) {
                    retVal.light = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("weather")) {
                    retVal.weather = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("wind")) {
                    retVal.wind = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("fog")) {
                    retVal.fog = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("atmosphere")) {
                    retVal.atmosphere = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("temperature")) {
                    retVal.temperature = Integer.parseInt(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("gravity")) {
                    retVal.gravity = Float.parseFloat(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("emi")) {
                    retVal.emi = Boolean.parseBoolean(wn2.getTextContent());
                } else if (wn2.getNodeName().equalsIgnoreCase("blowingSand")) {
                    retVal.blowingSand = Boolean.parseBoolean(wn2.getTextContent());
                }
            }
        } catch (Exception ex) {
            LogManager.getLogger().error(ex);
        }

        return retVal;
    }

    protected static List<String> getEntityStub(Node wn) {
        List<String> stub = new ArrayList<>();
        NodeList nl = wn.getChildNodes();
        for (int x = 0; x < nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            if (wn2.getNodeName().equalsIgnoreCase("entityStub")) {
                stub.add(MekHqXmlUtil.unEscape(wn2.getTextContent()));
            }
        }
        return stub;
    }

    public List<Loot> getLoot() {
        return loots;
    }

    public void addLoot(Loot l) {
        loots.add(l);
    }

    public void resetLoot() {
        loots = new ArrayList<>();
    }

    public boolean isFriendlyUnit(Entity entity, Campaign campaign) {
        return getForces(campaign).getUnits().stream().
                anyMatch(unitID -> unitID.equals(UUID.fromString(entity.getExternalIdAsString())));
    }
}
