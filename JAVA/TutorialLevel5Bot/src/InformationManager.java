import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Unitset;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;

/// 게임 상황정보 중 일부를 자체 자료구조 및 변수들에 저장하고 업데이트하는 class
/// 현재 게임 상황정보는 BWAPI::Broodwar 를 조회하여 파악할 수 있지만, 과거 게임 상황정보는 BWAPI::Broodwar 를 통해 조회가 불가능하기 때문에 InformationManager에서 별도 관리하도록 합니다
/// 또한, BWAPI::Broodwar 나 BWTA 등을 통해 조회할 수 있는 정보이지만 전처리 / 별도 관리하는 것이 유용한 것도 InformationManager에서 별도 관리하도록 합니다
public class InformationManager {
	private static InformationManager instance = new InformationManager();

	public Player selfPlayer;		///< 아군 Player		
	public Player enemyPlayer;		///< 아군 Player의 종족		
	public Race selfRace;			///< 적군 Player		
	public Race enemyRace;			///< 적군 Player의 종족  

	public List<Unit> validUnits = new ArrayList<Unit>();

	private Map<Player, BaseLocation> mainBaseLocations = new HashMap<Player, BaseLocation>();
	private Map<Player, List<BaseLocation>> occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();
	private Map<Player, Set<Region>> occupiedRegions = new HashMap<Player, Set<Region>>();

	private Map<Player, Chokepoint> firstChokePoint = new HashMap<Player, Chokepoint>();
	private Map<Player, BaseLocation> firstExpansionLocation = new HashMap<Player, BaseLocation>();
	private Map<Player, Chokepoint> secondChokePoint = new HashMap<Player, Chokepoint>();

	/// Player - UnitData(각 Unit 과 그 Unit의 UnitInfo 를 Map 형태로 저장하는 자료구조) 를 저장하는 자료구조 객체
	private Map<Player, UnitData> unitData = new HashMap<Player, UnitData>();

	/// static singleton 객체를 리턴합니다
	public static InformationManager Instance() {
		return instance;
	}

	public Player getSelf() {
		return selfPlayer;
	}

	public InformationManager() {
		selfPlayer = MyBotModule.Broodwar.self();
		enemyPlayer = MyBotModule.Broodwar.enemy();
		selfRace = selfPlayer.getRace();
		enemyRace = enemyPlayer.getRace();
		
		mainBaseLocations = new HashMap<Player, BaseLocation>();
		mainBaseLocations.put(selfPlayer, BWTA.getStartLocation(MyBotModule.Broodwar.self()));
		occupiedBaseLocations = new HashMap<Player, List<BaseLocation>>();
		occupiedBaseLocations.put(selfPlayer, new ArrayList<BaseLocation>());
		occupiedBaseLocations.get(selfPlayer).add(mainBaseLocations.get(selfPlayer));
		updateOccupiedRegions(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition()),
				MyBotModule.Broodwar.self());
		mainBaseLocations.put(enemyPlayer, null);
		occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>());
		firstChokePoint.put(selfPlayer, null);
		firstChokePoint.put(enemyPlayer, null);
		firstExpansionLocation.put(selfPlayer, null);
		firstExpansionLocation.put(enemyPlayer, null);
		secondChokePoint.put(selfPlayer, null);
		secondChokePoint.put(enemyPlayer, null);
		updateChokePointAndExpansionLocation();
		occupiedRegions.put(selfPlayer, new HashSet());
		occupiedRegions.put(enemyPlayer, new HashSet());
	}

	/// Unit 및 BaseLocation, ChokePoint 등에 대한 정보를 업데이트합니다
	public void update() {
		// 적군이 Eliminate 되거나 Left 했을 때, enemy 값은 자동으로 null 이 되지 않으므로, null 로
		// 만들어줘야 한다
		if (MyBotModule.Broodwar.enemy() == null) {
			enemyPlayer = null;
		} else {
			if (MyBotModule.Broodwar.enemy().isDefeated() || MyBotModule.Broodwar.enemy().leftGame()) {
				enemyPlayer = null;
			}
		}

		updateUnitsInfo();
		// occupiedBaseLocation 이나 occupiedRegion 은 거의 안바뀌므로 자주 안해도 된다
		if (MyBotModule.Broodwar.getFrameCount() % 120 == 0) {
			updateBaseLocationInfo();
		}
	}

	/// 전체 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitsInfo() {
		// update our units info

		validUnits.clear();
		setValidUnits();
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			updateUnitInfo(unit);
		}
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			updateUnitInfo(unit);
		}

		// remove bad enemy units
		if (unitData.get(enemyPlayer) != null) {
			unitData.get(enemyPlayer).removeBadUnits();
		}
		if (unitData.get(selfPlayer) != null) {
			unitData.get(selfPlayer).removeBadUnits();
		}
	}

	// validates units as usable for distribution to various managers
	public void setValidUnits() {
		// make sure the unit is completed and alive and usable
		for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
			if (unit == null) {
				continue;
			}

			if (unit.isCompleted() && unit.getHitPoints() > 0 && unit.exists() && unit.getType() != UnitType.Unknown
					&& unit.getPosition() != Position.Unknown) {
				validUnits.add(unit);
			}
		}
	}

	/// 해당 unit 의 정보를 업데이트 합니다 (UnitType, lastPosition, HitPoint 등)
	public void updateUnitInfo(Unit unit) {
		try {
			if (!(unit.getPlayer() == selfPlayer || unit.getPlayer() == enemyPlayer)) {
				return;
			}

			if (enemyRace == Race.Unknown && unit.getPlayer() == enemyPlayer) {
				enemyRace = unit.getType().getRace();
			}
			if (unitData.get(unit.getPlayer()) == null) {
				unitData.put(unit.getPlayer(), new UnitData());
			}
			unitData.get(unit.getPlayer()).updateUnitInfo(unit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// is the unit valid?
	boolean isValidUnit(Unit unit) {
		// we only care about our units and enemy units
		if (unit.getPlayer() != MyBotModule.Broodwar.self() && unit.getPlayer() != MyBotModule.Broodwar.enemy()) {
			return false;
		}

		// if it's a weird unit, don't bother
		if (unit.getType() == UnitType.None || unit.getType() == UnitType.Unknown
				|| unit.getType() == UnitType.Zerg_Larva || unit.getType() == UnitType.Zerg_Egg) {
			return false;
		}

		// if the position isn't valid throw it out
		if (!unit.getPosition().isValid()) {
			return false;
		}

		// s'all good baby baby
		return true;
	}

	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitShow(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitHide(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitCreate(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitComplete(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitMorph(Unit unit) { 
		updateUnitInfo(unit); 
	}
	/// Unit 에 대한 정보를 업데이트합니다
	public void onUnitRenegade(Unit unit) { 
		updateUnitInfo(unit); 
	}
	
	/// Unit 에 대한 정보를 업데이트합니다 
	/// 유닛이 파괴/사망한 경우, 해당 유닛 정보를 삭제합니다
	public void onUnitDestroy(Unit unit) {
		if (unit.getType().isNeutral()) {
			return;
		}

		unitData.get(unit.getPlayer()).removeUnit(unit);
	}


	/// 해당 Player (아군 or 적군) 의 position 주위의 유닛 목록을 unitInfo 에 저장합니다		 
	public void getNearbyForce(Vector<UnitInfo> unitInfo, Position p, Player player, int radius) {
		Iterator<Unit> it = getUnitData(player).getUnits().keySet().iterator();

		// for each unit we know about for that player
		// for (final Unit kv :
		// getUnitData(player).getUnits().keySet().iterator()){
		while (it.hasNext()) {
			Unit unit = it.next();
			final UnitInfo ui = getUnitData(player).getUnits().get(unit);

			// if it's a combat unit we care about
			// and it's finished!
			if (isCombatUnit(ui.getType()) && ui.isCompleted()) {
				// determine its attack range
				int range = 0;
				if (ui.getType().groundWeapon() != WeaponType.None) {
					range = ui.getType().groundWeapon().maxRange() + 40;
				}

				// if it can attack into the radius we care about
				if (ui.getLastPosition().getDistance(p) <= (radius + range)) {
					// add it to the vector
					// C++ : unitInfo.push_back(ui);
					unitInfo.add(ui);
				}
			} else if (ui.getType().isDetector() && ui.getLastPosition().getDistance(p) <= (radius + 250)) {
				// add it to the vector
				// C++ : unitInfo.push_back(ui);
				unitInfo.add(ui);
			}
		}
	}

	public Unit getClosestUnitToTarget(UnitType type, Position target) {
		Unit closestUnit = null;
		double closestDist = 100000;

		for (Unit unit : validUnits) {
			if (unit.getType() == type) {
				double dist = unit.getDistance(target);
				if (closestUnit == null || dist < closestDist) {
					closestUnit = unit;
					closestDist = dist;
				}
			}
		}

		return closestUnit;
	}

	/// 해당 Player (아군 or 적군) 의 해당 UnitType 유닛 숫자를 리턴합니다 (훈련/건설 중인 유닛 숫자까지 포함)
	public int getNumUnits(UnitType t, Player player) {
		return getUnitData(player).getNumUnits(t.toString());
	}

	/// 해당 Player (아군 or 적군) 의 모든 유닛 통계 UnitData 을 리턴합니다		 
	public final UnitData getUnitData(Player player) {
		return unitData.get(player);
	}

	public void updateBaseLocationInfo() {
		if (occupiedRegions.get(selfPlayer) != null) {
			occupiedRegions.get(selfPlayer).clear();
		}
		if (occupiedRegions.get(enemyPlayer) != null) {
			occupiedRegions.get(enemyPlayer).clear();
		}
		if (occupiedBaseLocations.get(selfPlayer) != null) {
			occupiedBaseLocations.get(selfPlayer).clear();
		}
		if (occupiedBaseLocations.get(enemyPlayer) != null) {
			occupiedBaseLocations.get(enemyPlayer).clear();
		}

		// enemy 의 startLocation을 아직 모르는 경우
		if (mainBaseLocations.get(enemyPlayer) == null) {
			// how many start locations have we explored
			int exploredStartLocations = 0;
			boolean enemyStartLocationFound = false;

			// an unexplored base location holder
			BaseLocation unexplored = null;

			for (BaseLocation startLocation : BWTA.getStartLocations()) {
				if (existsEnemyBuildingInRegion(BWTA.getRegion(startLocation.getTilePosition()))) {
					if (enemyStartLocationFound == false) {
						enemyStartLocationFound = true;
						mainBaseLocations.put(enemyPlayer, startLocation);
					}
				}

				if (MyBotModule.Broodwar.isExplored(startLocation.getTilePosition())) {
					// if it's explored, increment
					exploredStartLocations++;
				} else {
					// otherwise set it as unexplored base
					unexplored = startLocation;
				}
			}

			// if we've explored every start location except one, it's the enemy
			if (!enemyStartLocationFound && exploredStartLocations == ((int) BWTA.getStartLocations().size() - 1)) {
				enemyStartLocationFound = true;
				mainBaseLocations.put(enemyPlayer, unexplored);
				// C++ : _occupiedBaseLocations[_enemy].push_back(unexplored);
				if(occupiedBaseLocations.get(enemyPlayer) == null)
				{
					occupiedBaseLocations.put(enemyPlayer, new ArrayList<BaseLocation>()); 
				}
				occupiedBaseLocations.get(enemyPlayer).add(unexplored);
			}
		}

		// update occupied base location
		// 어떤 Base Location 에는 아군 건물, 적군 건물 모두 혼재해있어서 동시에 여러 Player 가 Occupy 하고
		// 있는 것으로 판정될 수 있다
		for (BaseLocation baseLocation : BWTA.getBaseLocations()) {
			if (hasBuildingAroundBaseLocation(baseLocation, enemyPlayer)) {
				// C++ : _occupiedBaseLocations[_enemy].push_back(baseLocation);
				occupiedBaseLocations.get(enemyPlayer).add(baseLocation);
			}

			if (hasBuildingAroundBaseLocation(baseLocation, selfPlayer)) {
				// C++ : _occupiedBaseLocations[_self].push_back(baseLocation);
				occupiedBaseLocations.get(selfPlayer).add(baseLocation);
			}
		}

		// enemy의 mainBaseLocations을 발견한 후, 그곳에 있는 건물을 모두 파괴한 경우
		// _occupiedBaseLocations 중에서 _mainBaseLocations 를 선정한다
		if (mainBaseLocations.get(enemyPlayer) != null) {
			if (existsEnemyBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(enemyPlayer).getTilePosition())) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(enemyPlayer)) {
					if (existsEnemyBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()))) {
						mainBaseLocations.put(enemyPlayer, loaction);
						break;
					}
				}
			}
		}

		// self의 mainBaseLocations에 대해, 그곳에 있는 건물이 모두 파괴된 경우
		// _occupiedBaseLocations 중에서 _mainBaseLocations 를 선정한다
		if (mainBaseLocations.get(selfPlayer) != null) {
			if (existsEnemyBuildingInRegion(BWTA.getRegion(mainBaseLocations.get(selfPlayer).getTilePosition())) == false) {
				for (BaseLocation loaction : occupiedBaseLocations.get(selfPlayer)) {
					if (existsEnemyBuildingInRegion(BWTA.getRegion(loaction.getTilePosition()))) {
						mainBaseLocations.put(selfPlayer, loaction);
						break;
					}
				}
			}
		}

		Iterator<Unit> it = null;
		if (unitData.get(enemyPlayer) != null) {
			it = unitData.get(enemyPlayer).getUnits().keySet().iterator();

			// for each enemy building unit we know about
			// for (const auto & kv : unitData.get(enemy).getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(enemyPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.enemy());
				}
			}
		}

		if (unitData.get(selfPlayer) != null) {
			it = unitData.get(selfPlayer).getUnits().keySet().iterator();

			// for each of our building units
			// for (const auto & kv : _unitData[_self].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(selfPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					updateOccupiedRegions(BWTA.getRegion(ui.getLastPosition().toTilePosition()),
							MyBotModule.Broodwar.self());
				}
			}
		}

		updateChokePointAndExpansionLocation();
	}

	public void updateChokePointAndExpansionLocation() {
		if (mainBaseLocations.get(selfPlayer) != null) {
			BaseLocation sourceBaseLocation = mainBaseLocations.get(selfPlayer);

			// 맵 중앙 혹은 상대편 기지를 기준으로 FirstChokePoint, SecondChokePoint 산정
			Position targetPosition = new Position(MyBotModule.Broodwar.mapWidth() * Config.TILE_SIZE / 2,
					MyBotModule.Broodwar.mapHeight() * Config.TILE_SIZE / 2);
			if (mainBaseLocations.get(enemyPlayer) != null) {
				targetPosition = mainBaseLocations.get(enemyPlayer).getPosition();
			}

			if (BWTA.isConnected(sourceBaseLocation.getTilePosition(), targetPosition.toTilePosition())) {

				firstChokePoint.put(selfPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));

				if (firstChokePoint.get(selfPlayer) != null) {

					firstExpansionLocation.put(selfPlayer,
							BWTA.getNearestBaseLocation(firstChokePoint.get(selfPlayer).getCenter().toTilePosition()));

					if (firstExpansionLocation.get(selfPlayer) != null) {
						double tempDistance;
						double closestDistance = 100000000;
						TilePosition tempTilePosition;
						for (Chokepoint chokePoint : BWTA.getChokepoints()) {
							if (chokePoint != BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition())) {
								tempDistance = BWTA.getGroundDistance(chokePoint.getCenter().toTilePosition(),
										firstExpansionLocation.get(selfPlayer).getTilePosition());
								if (tempDistance < closestDistance) {
									closestDistance = tempDistance;
									secondChokePoint.put(selfPlayer, chokePoint);
								}
							}
						}
					}
				}
			}
		}

		if (mainBaseLocations.get(enemyPlayer) != null) {
			BaseLocation sourceBaseLocation = mainBaseLocations.get(enemyPlayer);

			// 맵 중앙 혹은 상대편 기지를 기준으로 FirstChokePoint, SecondChokePoint 산정
			Position targetPosition = new Position(MyBotModule.Broodwar.mapWidth() * Config.TILE_SIZE / 2,
					MyBotModule.Broodwar.mapHeight() * Config.TILE_SIZE / 2);
			if (mainBaseLocations.get(selfPlayer) != null) {
				targetPosition = mainBaseLocations.get(selfPlayer).getPosition();
			}

			if (BWTA.isConnected(sourceBaseLocation.getTilePosition(), targetPosition.toTilePosition())) {

				firstChokePoint.put(enemyPlayer, BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition()));

				if (firstChokePoint.get(enemyPlayer) != null) {

					firstExpansionLocation.put(enemyPlayer,
							BWTA.getNearestBaseLocation(firstChokePoint.get(enemyPlayer).getCenter()));

					if (firstExpansionLocation.get(enemyPlayer) != null) {
						double tempDistance;
						double closestDistance = 100000000;
						TilePosition tempTilePosition;
						for (Chokepoint chokePoint : BWTA.getChokepoints()) {
							if (chokePoint != BWTA.getNearestChokepoint(sourceBaseLocation.getTilePosition())) {
								tempDistance = BWTA.getGroundDistance(chokePoint.getCenter().toTilePosition(),
										firstExpansionLocation.get(enemyPlayer).getTilePosition());
								if (tempDistance < closestDistance) {
									closestDistance = tempDistance;
									secondChokePoint.put(enemyPlayer, chokePoint);
								}
							}
						}
					}
				}
			}
		}
	}

	public void updateOccupiedRegions(Region region, Player player) {
		// if the region is valid (flying buildings may be in null regions)
		if (region != null) {
			// add it to the list of occupied regions
			if (occupiedRegions.get(player) == null) {
				occupiedRegions.put(player, new HashSet<Region>());
			}
			occupiedRegions.get(player).add(region);
		}
	}

	/// 해당 BaseLocation 에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	/// @param radius TilePosition 단위
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player, int radius) {

		// invalid regions aren't considered the same, but they will both be null
		if (baseLocation == null) {
			return false;
		}

		// 반지름 10 (TilePosition 단위) 이면 거의 화면 가득이다
		int maxRadius = 10;

		if (unitData.get(player) != null) {
			Iterator<Unit> it = unitData.get(player).getUnits().keySet().iterator();

			// for (const auto & kv : _unitData[player].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(player).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					TilePosition buildingPosition = ui.getLastPosition().toTilePosition();

					if (buildingPosition.getX() >= baseLocation.getTilePosition().getX() - maxRadius
							&& buildingPosition.getX() <= baseLocation.getTilePosition().getX() + maxRadius
							&& buildingPosition.getY() >= baseLocation.getTilePosition().getY() - maxRadius
							&& buildingPosition.getY() <= baseLocation.getTilePosition().getY() + maxRadius) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/// 해당 BaseLocation 주위 10타일 반경 내에 player의 건물이 존재하는지 리턴합니다
	/// @param baseLocation 대상 BaseLocation
	/// @param player 아군 / 적군
	public boolean hasBuildingAroundBaseLocation(BaseLocation baseLocation, Player player) {
		return hasBuildingAroundBaseLocation(baseLocation, player, 10);
	}


	/// 해당 Region 에 적군 건물이 존재하는지 리턴합니다
	public boolean existsEnemyBuildingInRegion(Region region) {
		// invalid regions aren't considered the same, but they will both be
		// null
		if (region == null) {
			return false;
		}

		if (unitData.get(enemyPlayer) != null) {
			Iterator<Unit> it = unitData.get(enemyPlayer).getUnits().keySet().iterator();

			// for (const auto & kv : unitData[_enemy].getUnits())
			while (it.hasNext()) {
				Unit unit = it.next();
				final UnitInfo ui = unitData.get(enemyPlayer).getUnits().get(unit);
				if (ui.getType().isBuilding()) {
					if (BWTA.getRegion(ui.getLastPosition()) == region) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/// 해당 Region 에 아군 건물이 존재하는지 리턴합니다
	public boolean existsMyBuildingInRegion(Region region) {
		// invalid regions aren't considered the same, but they will both be
		// null
		if (region == null) {
			return false;
		}

		Iterator<Unit> it = unitData.get(selfPlayer).getUnits().keySet().iterator();

		// for (const auto & kv : unitData.get(self).getUnits())
		while (it.hasNext()) {
			Unit unit = it.next();
			final UnitInfo ui = unitData.get(selfPlayer).getUnits().get(unit);
			if (ui.getType().isBuilding() && ui.isCompleted()) {
				if (BWTA.getRegion(ui.getLastPosition()) == region) {
					return true;
				}
			}
		}

		return false;
	}

	/// 해당 Player (아군 or 적군) 의 모든 유닛 목록 (가장 최근값) UnitAndUnitInfoMap 을 리턴합니다		 
	/// 파악된 정보만을 리턴하기 때문에 적군의 정보는 틀린 값일 수 있습니다
	public final Map<Unit, UnitInfo> getUnitInfo(Player player) {
		return getUnitData(player).getUnits();
	}

	/// 해당 Player (아군 or 적군) 가 건물을 건설해서 점령한 Region 목록을 리턴합니다
	public Set<Region> getOccupiedRegions(Player player) {
		return occupiedRegions.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 건물을 건설해서 점령한 BaseLocation 목록을 리턴합니다		 
	public List<BaseLocation> getOccupiedBaseLocations(Player player) {
		return occupiedBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 을 리턴합니다		 
	public BaseLocation getMainBaseLocation(Player player) {
		return mainBaseLocations.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 ChokePoint 를 리턴합니다		 
	public Chokepoint getFirstChokePoint(Player player) {
		return firstChokePoint.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 가장 가까운 Expansion BaseLocation 를 리턴합니다		 
	public BaseLocation getFirstExpansionLocation(Player player) {
		return firstExpansionLocation.get(player);
	}

	/// 해당 Player (아군 or 적군) 의 Main BaseLocation 에서 두번째로 가까운 ChokePoint 를 리턴합니다		 
	public Chokepoint getSecondChokePoint(Player player) {
		return secondChokePoint.get(player);
	}

	/// 해당 UnitType 이 전투 유닛인지 리턴합니다
	public final boolean isCombatUnit(UnitType type) {
		if (type == UnitType.Zerg_Lurker/*
										 * || type == BWAPI::UnitTypes::
										 * Protoss_Dark_Templar
										 */) {
			return false;
		}

		// check for various types of combat units
		if (type.canAttack() || type == UnitType.Terran_Medic || type == UnitType.Protoss_Observer
				|| type == UnitType.Terran_Bunker) {
			return true;
		}

		return false;
	}
	
	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType() {
		return getBasicCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getBasicCombatUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Zealot;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Marine;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Zergling;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType() {
		return getAdvancedCombatUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Combat Unit 에 해당하는 UnitType을 리턴합니다
	public UnitType getAdvancedCombatUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Dragoon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Medic;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hydralisk;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType() {
		return getBasicCombatBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Combat Unit 을 생산하기 위해 건설해야하는 UnitType을 리턴합니다
	public UnitType getBasicCombatBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Gateway;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Barracks;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType() {
		return getObserverUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Observer 에 해당하는 UnitType을 리턴합니다
	public UnitType getObserverUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Observer;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Science_Vessel;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Overlord;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType() {
		return getBasicResourceDepotBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 ResourceDepot 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicResourceDepotBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Nexus;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Command_Center;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Hatchery;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType() {
		return getRefineryBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Refinery 기능을 하는 UnitType을 리턴합니다
	public UnitType getRefineryBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Assimilator;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Refinery;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Extractor;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType() {
		return getWorkerType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Worker 에 해당하는 UnitType을 리턴합니다
	public UnitType getWorkerType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Probe;
		} else if (race == Race.Terran) {
			return UnitType.Terran_SCV;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Drone;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType() {
		return getBasicSupplyProviderUnitType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 SupplyProvider 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicSupplyProviderUnitType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Pylon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Supply_Depot;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Overlord;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType() {
		return getBasicDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Basic Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getBasicDefenseBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Pylon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Bunker;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Creep_Colony;
		} else {
			return UnitType.None;
		}
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType() {
		return getAdvancedDefenseBuildingType(MyBotModule.Broodwar.self().getRace());
	}

	// 해당 종족의 UnitType 중 Advanced Depense 기능을 하는 UnitType을 리턴합니다
	public UnitType getAdvancedDefenseBuildingType(Race race) {
		if (race == Race.Protoss) {
			return UnitType.Protoss_Photon_Cannon;
		} else if (race == Race.Terran) {
			return UnitType.Terran_Missile_Turret;
		} else if (race == Race.Zerg) {
			return UnitType.Zerg_Sunken_Colony;
		} else {
			return UnitType.None;
		}
	}
}