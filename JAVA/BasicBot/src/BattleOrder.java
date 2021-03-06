import java.util.Iterator;
import java.util.List;

import bwapi.Position;
import bwapi.Region;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.WeaponType;
import bwta.BaseLocation;
import bwta.Chokepoint;

public abstract class BattleOrder {
	public static final int TOTAL_RADIUS = 1000;
	public static final int BASE_RADIUS = 500;
	public static final int CORSAIR_RADIUS = 100;
	public static final int STUCK_RADIUS = 50;
	public static final int ENEMY_RADIUS = 50;
	
	protected abstract void execute();
	protected abstract void changeBattleMode();
	
	protected void changeLeaderPeriodically() {
		if (MyBotModule.Broodwar.getFrameCount() % 720 != 0) {
			return;
		}
		BattleUnitGroup zealotFrontGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleGroupType.FRONT_GROUP.getValue());
		BattleManager.changeLeaderForce(zealotFrontGroup.getLeader(), zealotFrontGroup);
		BattleUnitGroup zealotSubGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleGroupType.SUB_GROUP.getValue());
		BattleManager.changeLeaderForce(zealotSubGroup.getLeader(), zealotSubGroup);
		BattleUnitGroup dragoonFrontGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue());
		BattleManager.changeLeaderForce(dragoonFrontGroup.getLeader(), dragoonFrontGroup);
		BattleUnitGroup dragoonSubGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.SUB_GROUP.getValue());
		BattleManager.changeLeaderForce(dragoonSubGroup.getLeader(), dragoonSubGroup);
	}
	
	protected void moveStuckDragoon() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		BattleUnit shuttle = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Shuttle).getLeader();
		if (shuttle != null) {
			if (shuttle.getUnit().exists()) {
				this.loadStuckLeader(shuttle, UnitType.Protoss_Dragoon, BattleGroupType.FRONT_GROUP);
				this.loadStuckLeader(shuttle, UnitType.Protoss_Dragoon, BattleGroupType.SUB_GROUP);
				this.loadStuckLeader(shuttle, UnitType.Protoss_Zealot, BattleGroupType.FRONT_GROUP);
				this.loadStuckLeader(shuttle, UnitType.Protoss_Zealot, BattleGroupType.SUB_GROUP);

				BuildingUnitGroup gatewayGroup = BuildingUnitManager.instance().getBuildingUnitGroup(UnitType.Protoss_Gateway);
				gateloop : for (int unitId : gatewayGroup.buildingUnitGroup.keySet()) {
					BuildingUnit gateway = gatewayGroup.buildingUnitGroup.get(unitId);
					for (Unit unit : MyBotModule.Broodwar.getUnitsInRadius(gateway.getUnit().getPosition(), STUCK_RADIUS)) {
						if (unit.getType() == UnitType.Protoss_Dragoon) {
							if (shuttle.getUnit().getSpaceRemaining() != 0) {
								shuttle.getUnit().load(unit);
							} else {
								break gateloop;
							}
						}
					}
				}
				BuildingUnitGroup assimilatorGroup = BuildingUnitManager.instance().getBuildingUnitGroup(UnitType.Protoss_Assimilator);
				assimilatorloop : for (int unitId : assimilatorGroup.buildingUnitGroup.keySet()) {
					BuildingUnit assimilator = assimilatorGroup.buildingUnitGroup.get(unitId);
					for (Unit unit : MyBotModule.Broodwar.getUnitsInRadius(assimilator.getUnit().getPosition(), STUCK_RADIUS)) {
						if (unit.getType() == UnitType.Protoss_Dragoon) {
							if (shuttle.getUnit().getSpaceRemaining() != 0) {
								shuttle.getUnit().load(unit);
							} else {
								break assimilatorloop;
							}
						}
					}
				}
				Chokepoint secondChokepoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
				shuttle.getUnit().unloadAll(secondChokepoint.getCenter(), true);
			} else {
				BattleManager.changeLeader(shuttle, BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Shuttle));
			}
		}
	}
	
	private void loadStuckLeader(BattleUnit shuttle, UnitType unitType, BattleGroupType battleGroupType) {
		BattleUnitGroup battleUnitGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(unitType).get(battleGroupType.getValue());
		BaseLocation selfBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
		BattleUnit leader = battleUnitGroup.getLeader();
		if (leader != null && leader.getUnit().getDistance(selfBaseLocation.getPosition()) < UnitType.Protoss_Nexus.sightRange()) {
			if (shuttle.getUnit().getSpaceRemaining() != 0) {
				shuttle.getUnit().load(leader.getUnit());
			}
		}
	}
	
	protected void observing() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		BattleUnitGroup observerGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Observer);	
		BattleUnit observerLeader = observerGroup.getLeader();
		if (observerLeader != null) {
			if (observerGroup.getUnitCount() > 0 && observerGroup.getUnitCount() < 5) {
				Iterator<Integer> iterator = observerGroup.battleUnits.keySet().iterator();
				BattleUnit observer = observerGroup.battleUnits.get(iterator.next());
				BattleUnit leader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
				this.unitFollow(observer, leader);
				if (iterator.hasNext()) {
					observer = observerGroup.battleUnits.get(iterator.next());
					leader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleGroupType.SUB_GROUP.getValue()).getLeader();
					this.unitFollow(observer, leader);
				}
				if (iterator.hasNext()) {
					BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
					if (enemyBaseLocation != null) {
						List<TilePosition> centerExpansionNearEnemy = ProtossBasicBuildPosition.Instance().getCenterExpansionNearEnemy(enemyBaseLocation.getTilePosition());
						if (!centerExpansionNearEnemy.isEmpty()) {
							observer = observerGroup.battleUnits.get(iterator.next());
							if (observer.getUnit().isUnderAttack()) {
								observer.move(ProtossBasicBuildPosition.mapInfo.get(ProtossBasicBuildPosition.CENTER).toPosition());
							} else {
								CommandUtil.move(observer.getUnit(), centerExpansionNearEnemy.get(0).toPosition());	
							}
							if (iterator.hasNext()) {
								observer = observerGroup.battleUnits.get(iterator.next());
								if (observer.getUnit().isUnderAttack()) {
									observer.move(ProtossBasicBuildPosition.mapInfo.get(ProtossBasicBuildPosition.CENTER).toPosition());
								} else {
									CommandUtil.move(observer.getUnit(), centerExpansionNearEnemy.get(1).toPosition());	
								}
							}
						}
					}
				}
			} else {
				if (observerLeader.getUnit().exists()) {
					for (String base : ProtossBasicBuildPosition.Instance().getScoutPositions().keySet()) {
						TilePosition tilePosition = ProtossBasicBuildPosition.Instance().getScoutPositions().get(base);
						if (observerLeader.getUnit().getDistance(tilePosition.toPosition()) > 50 && !observerLeader.getUnit().isMoving()) {
							observerLeader.getUnit().move(tilePosition.toPosition(), true);
						}
					}
				}
			}
		}
	}
	
	protected void unitFollow(BattleUnit follower, Unit scoutUnit) {
		if (follower != null) {
			if (scoutUnit != null) {
				if (!follower.getUnit().isFollowing() && follower.getUnit().canFollow(scoutUnit)) {
					follower.getUnit().follow(scoutUnit);
				}
			}
		}
	}
	
	protected void unitFollow(BattleUnit follower, BattleUnit battleUnit) {
		if (follower != null) {
			if (battleUnit != null) {
				if (!follower.getUnit().isFollowing() && follower.getUnit().canFollow(battleUnit.getUnit())) {
					follower.getUnit().follow(battleUnit.getUnit());
				}
			}
		}
	}
	
	protected void formationAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		if (BattleManager.instance().getBattleMode() != BattleManager.BattleMode.ONEWAY_ATTACK &&
				BattleManager.instance().getBattleMode() != BattleManager.BattleMode.ELEMINATE) {
			BattleManager.instance().makeFormation(UnitType.Protoss_Zealot, BattleGroupType.FRONT_GROUP);
			BattleManager.instance().makeFormation(UnitType.Protoss_Dragoon, BattleGroupType.FRONT_GROUP);
			BattleManager.instance().makeFormation(UnitType.Protoss_Zealot, BattleGroupType.SUB_GROUP);
			BattleManager.instance().makeFormation(UnitType.Protoss_Dragoon, BattleGroupType.SUB_GROUP);
			BattleManager.instance().makeFormation(UnitType.Protoss_Zealot, BattleGroupType.DEFENCE_GROUP);
			BattleManager.instance().makeFormation(UnitType.Protoss_Dragoon, BattleGroupType.DEFENCE_GROUP);
		}
	}
	
	protected void enemyExpansionAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		if (BattleManager.instance().getBattleMode() == BattleManager.BattleMode.WAIT) {
			int frontCount = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getUnitCount();
			int subCount = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.SUB_GROUP.getValue()).getUnitCount();
			if (frontCount+subCount > 3) {
				BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
				if (enemyBaseLocation != null) {
					BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.enemy());
					BattleUnit subLeader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.SUB_GROUP.getValue()).getLeader();
					if (subLeader != null && subLeader.getUnit().isUnderAttack() && subLeader.getUnit().isAttacking()) {
						BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, subLeader.getUnit().getPosition(), BattleGroupType.FRONT_GROUP);
						BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, subLeader.getUnit().getPosition(), BattleGroupType.FRONT_GROUP);
					} else {
						BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, ProtossBasicBuildPosition.Instance().getEnemyDefencePosition(enemyBaseLocation.getTilePosition()).toPosition(), BattleGroupType.FRONT_GROUP);
						BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, ProtossBasicBuildPosition.Instance().getEnemyDefencePosition(enemyBaseLocation.getTilePosition()).toPosition(), BattleGroupType.FRONT_GROUP);
					}
					BattleUnit frontLeader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
					if (frontLeader != null && frontLeader.getUnit().isUnderAttack() && frontLeader.getUnit().isAttacking()) {
						BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, frontLeader.getUnit().getPosition(), BattleGroupType.SUB_GROUP);
						BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, frontLeader.getUnit().getPosition(), BattleGroupType.SUB_GROUP);
					} else if (!this.centerExpansionAttack(BattleGroupType.SUB_GROUP)) {
						BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyFirstExpansionLocation.getPosition(), BattleGroupType.SUB_GROUP);
						BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyFirstExpansionLocation.getPosition(), BattleGroupType.SUB_GROUP);	
					}
				}
			} else {
				Chokepoint selfChokepoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, selfChokepoint.getCenter(), BattleGroupType.FRONT_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, selfChokepoint.getCenter(), BattleGroupType.FRONT_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, selfChokepoint.getCenter(), BattleGroupType.SUB_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, selfChokepoint.getCenter(), BattleGroupType.SUB_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, selfChokepoint.getCenter(), BattleGroupType.DEFENCE_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, selfChokepoint.getCenter(), BattleGroupType.DEFENCE_GROUP);
			}
		}
	}
	
	protected boolean centerExpansionAttack(BattleGroupType battleGroupType) {
		BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
		if (enemyBaseLocation != null) {
			List<TilePosition> centerExpansionNearEnemy = ProtossBasicBuildPosition.Instance().getCenterExpansionNearEnemy(enemyBaseLocation.getTilePosition());
			Position centerExpansionBefore = centerExpansionNearEnemy.get(0).toPosition();
			Position centerExpansionAtfer = centerExpansionNearEnemy.get(1).toPosition();
			if (isEnemyInCenterExpansion(centerExpansionBefore)) {
//				System.out.println("[centerExpantionAttack] before");
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, centerExpansionBefore, battleGroupType);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, centerExpansionBefore, battleGroupType);
				return true;
			} else if (isEnemyInCenterExpansion(centerExpansionAtfer)) {
//				System.out.println("[centerExpantionAttack] after");
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, centerExpansionAtfer, battleGroupType);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, centerExpansionAtfer, battleGroupType);
				return true;
			}
		}
		return false;
	}
	
	private boolean isEnemyInCenterExpansion(Position centerExpansion) {
		boolean isEnemyInCenterExpansion = false;
		for (Unit unit : MyBotModule.Broodwar.getUnitsInRadius(centerExpansion, CommandUtil.UNIT_RADIUS)) {
			if (unit.getPlayer() == MyBotModule.Broodwar.enemy()) {
				isEnemyInCenterExpansion = true;
				break;
			}
		}
		return isEnemyInCenterExpansion;
	}
	
	protected void detectEnemyInSelf() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		boolean isEnemyAttack = false;
		for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(MyBotModule.Broodwar.self())) {
			if (baseLocation != null && this.detectEnemyAttack(baseLocation.getPosition())) {
				isEnemyAttack = true;
			}
		}
		
		if (isEnemyAttack) {
			BattleManager.instance().setBattleMode(BattleManager.BattleMode.DEFENCE);
		} else {
			this.changeBattleMode();
		}
	}
	
	protected boolean detectEnemyAttack(Position target) {
		boolean isFlyingAttack = false;
		boolean isEnemyAttack = false;
		List<Unit> list = MyBotModule.Broodwar.getUnitsInRadius(target, BASE_RADIUS);
		Position enemyPosition = null;
		for (Unit unit : list) {
			if (unit.getPlayer() == InformationManager.Instance().enemyPlayer &&
				!unit.getType().isWorker() && unit.exists()) {
				isEnemyAttack = true;
				if (unit.getType().isFlyer()) {
					isFlyingAttack = true;
				}
				enemyPosition = unit.getPosition();
				break;
			} 
		}
		if (isEnemyAttack) {
			if (!isFlyingAttack) {
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyPosition, BattleGroupType.DEFENCE_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyPosition, BattleGroupType.SUB_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyPosition, BattleGroupType.FRONT_GROUP);
			}
			BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyPosition, BattleGroupType.DEFENCE_GROUP);
			BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyPosition, BattleGroupType.SUB_GROUP);
			BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyPosition, BattleGroupType.FRONT_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Zealot, BattleGroupType.DEFENCE_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Dragoon, BattleGroupType.DEFENCE_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Zealot, BattleGroupType.SUB_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Dragoon, BattleGroupType.SUB_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Zealot, BattleGroupType.FRONT_GROUP);
			BattleManager.instance().closestAttack(UnitType.Protoss_Dragoon, BattleGroupType.FRONT_GROUP);
			return true;
		} else {
			return false;
		}
	}
	
	protected void onewayAttack(BaseLocation enemyLocation) {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		if (BattleManager.instance().getBattleMode() == BattleManager.BattleMode.ONEWAY_ATTACK) {
			if (enemyLocation != null) {
				TilePosition enemyBasePosition = ProtossBasicBuildPosition.Instance().getEnemyBase(enemyLocation.getTilePosition());
				if (enemyBasePosition == null) {
					enemyBasePosition = enemyLocation.getTilePosition();
				}
				Position targetUnit = null;
				for (Unit unit : MyBotModule.Broodwar.getUnitsInRadius(enemyBasePosition.toPosition(), TOTAL_RADIUS)) {
					if (unit.getPlayer() == MyBotModule.Broodwar.enemy() &&
							unit.getType().isBuilding()) {
						targetUnit = unit.getPosition();
						break;
					}
				}
				if (targetUnit == null) {
					BattleManager.instance().onewayAttack(UnitType.Protoss_Zealot, BattleGroupType.FRONT_GROUP, enemyBasePosition.toPosition());
					BattleManager.instance().onewayAttack(UnitType.Protoss_Dragoon, BattleGroupType.FRONT_GROUP, enemyBasePosition.toPosition());
					BattleManager.instance().onewayAttack(UnitType.Protoss_Zealot, BattleGroupType.SUB_GROUP, enemyBasePosition.toPosition());
					BattleManager.instance().onewayAttack(UnitType.Protoss_Dragoon, BattleGroupType.SUB_GROUP, enemyBasePosition.toPosition());
				} else {
					BattleManager.instance().onewayAttack(UnitType.Protoss_Zealot, BattleGroupType.FRONT_GROUP, targetUnit);
					BattleManager.instance().onewayAttack(UnitType.Protoss_Dragoon, BattleGroupType.FRONT_GROUP, targetUnit);
					BattleManager.instance().onewayAttack(UnitType.Protoss_Zealot, BattleGroupType.SUB_GROUP, targetUnit);
					BattleManager.instance().onewayAttack(UnitType.Protoss_Dragoon, BattleGroupType.SUB_GROUP, targetUnit);
				}
			}
		}
	}
	
	protected void totalAttack(BaseLocation enemyLocation) {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;	
		}
		
		if (BattleManager.instance().getBattleMode() == BattleManager.BattleMode.TOTAL_ATTACK) {
			if (enemyLocation != null) {
				TilePosition enemyBasePosition = ProtossBasicBuildPosition.Instance().getEnemyBase(enemyLocation.getTilePosition());
				if (enemyBasePosition == null) {
					enemyBasePosition = enemyLocation.getTilePosition();
				}
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyBasePosition.toPosition(), BattleGroupType.FRONT_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyBasePosition.toPosition(), BattleGroupType.FRONT_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, enemyBasePosition.toPosition(), BattleGroupType.SUB_GROUP);
				BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, enemyBasePosition.toPosition(), BattleGroupType.SUB_GROUP);
				if (ScoutManager.Instance().getScoutUnit() != null && ScoutManager.Instance().getScoutUnit().exists()) {
					Region region = ScoutManager.Instance().getScoutUnit().getRegion();
					BattleManager.instance().leaderAttack(UnitType.Protoss_Zealot, new Position(region.getBoundsLeft(), region.getBoundsTop()), BattleGroupType.DEFENCE_GROUP);
					BattleManager.instance().leaderAttack(UnitType.Protoss_Dragoon, new Position(region.getBoundsRight(), region.getBoundsBottom()), BattleGroupType.DEFENCE_GROUP);	
				}
			}
		}
	}
	
	protected void darkTemplarAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		BattleUnitGroup darkTemplarGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Dark_Templar);
		if (darkTemplarGroup.getUnitCount() > 0) {
			BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
			if (enemyBaseLocation != null) {
				for (int unitId : darkTemplarGroup.battleUnits.keySet()) {
					BattleUnit darkTemplar = darkTemplarGroup.battleUnits.get(unitId);
					Unit targetUnit = null;
					boolean isDetector = false;
					for (Unit unit : darkTemplar.getUnit().getUnitsInRadius(CommandUtil.UNIT_RADIUS)) {
						if (unit.getPlayer() == MyBotModule.Broodwar.enemy()) {
							if (unit.getType() == UnitType.Protoss_Nexus ||
								unit.getType() == UnitType.Protoss_Robotics_Facility ||
								unit.getType() == UnitType.Protoss_Observatory ||
								unit.getType() == UnitType.Protoss_Probe ||
								unit.getType() == UnitType.Terran_Missile_Turret ||
								unit.getType() == UnitType.Terran_Comsat_Station ||
								unit.getType() == UnitType.Terran_SCV) {
								targetUnit = unit;
								break;
							} else if (unit.getType() == UnitType.Protoss_Photon_Cannon) {
								isDetector = true;
								break;
							}
						}
					}
					
					if (isDetector) {
						BattleUnit leader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
						if (leader != null) {
							darkTemplar.getUnit().rightClick(leader.getUnit().getPosition());
						} else {
							Chokepoint selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
							darkTemplar.getUnit().rightClick(selfSecondChokePoint.getCenter());
						}
					} else {
						if (darkTemplar.getUnit().isUnderAttack()) {
							BattleUnit leader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
							if (leader != null) {
								darkTemplar.getUnit().rightClick(leader.getUnit().getPosition());
							} else {
								Chokepoint selfSecondChokePoint = InformationManager.Instance().getSecondChokePoint(MyBotModule.Broodwar.self());
								darkTemplar.getUnit().rightClick(selfSecondChokePoint.getCenter());
							}
						} else {
							if (targetUnit == null) {
								CommandUtil.attackMove(darkTemplar.getUnit(), enemyBaseLocation.getPosition());
							} else {
								CommandUtil.attackUnit(darkTemplar.getUnit(), targetUnit);
							}
						}
					}
				}
			}
		}
	}
	
	protected void trainWeaponAttack(UnitType unitType) {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		BattleUnitGroup battleUnitGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(unitType);
		if (battleUnitGroup.getUnitCount() > 0) {
			for (int unitId : battleUnitGroup.battleUnits.keySet()) {
				BattleUnit battleUnit = battleUnitGroup.battleUnits.get(unitId);
				((WeaponTrainable) battleUnit).train();
				
				if (battleUnit.getUnit().isUnderAttack()) {
					BaseLocation selfMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self());
					battleUnit.getUnit().rightClick(selfMainBaseLocation.getPosition());
				} else {
					Unit enemy = null;
					for (Unit unit : battleUnit.getUnit().getUnitsInRadius(UnitType.Protoss_Carrier.groundWeapon().maxRange())) {
						if (unit.getPlayer() == MyBotModule.Broodwar.enemy()) {
							if (unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode ||
									unit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode ||
									unit.getType() == UnitType.Terran_SCV) {
								enemy = unit;
								break;
							}
						}
					}
					if (enemy != null) {
						if (unitType == UnitType.Protoss_Reaver && battleUnit.getUnit().getScarabCount() > 0) {
							battleUnit.getUnit().attack(enemy);
						} else if (unitType == UnitType.Protoss_Carrier && battleUnit.getUnit().getInterceptorCount() > 5) {
							BattleManager.instance().setBattleMode(BattleManager.BattleMode.CARRIER_ATTACK);
							if (!battleUnit.getUnit().isAttacking() && !battleUnit.getUnit().isAttackFrame()) {
								battleUnit.getUnit().patrol(enemy.getPosition());
								for (Unit interceptor : battleUnit.getUnit().getInterceptors()) {
									interceptor.attack(enemy);
								}
							}
						}
					} else {
						if (unitType == UnitType.Protoss_Carrier && battleUnit.getUnit().getInterceptorCount() > 5) {
							BattleManager.instance().setBattleMode(BattleManager.BattleMode.CARRIER_ATTACK);
							BattleUnitGroup frontDragoonGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue());
							BattleUnit frontDragoon = frontDragoonGroup.getLeader();
							if (frontDragoon != null) {
								CommandUtil.patrolMove(battleUnit.getUnit(), frontDragoon.getUnit().getPosition());
							} else {
								if (BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Carrier).getUnitCount() > 5) {
									BaseLocation enemyMainBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
									CommandUtil.patrolMove(battleUnit.getUnit(), enemyMainBaseLocation.getPosition());
								}
							}
						}
					}
				}
			}
		} else {
			BattleManager.instance().setBattleMode(BattleManager.BattleMode.WAIT);
		}
	}
	
	protected void arbiterAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		BattleUnitGroup frontDragoonGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue());
		BattleUnit frontDragoon = frontDragoonGroup.getLeader();
		BattleUnitGroup subDragoonGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.SUB_GROUP.getValue());
		BattleUnit subDragoon = subDragoonGroup.getLeader();
		BattleUnitGroup arbiterGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Arbiter);
		if (arbiterGroup.getUnitCount() > 0) {
			Iterator<Integer> iterator = arbiterGroup.battleUnits.keySet().iterator();
			while (iterator.hasNext()) {
				BattleUnit arbiter = arbiterGroup.battleUnits.get(iterator.next());
				this.arbiterRecallAttack(arbiter);
				this.arbiterStatisFieldAttack(arbiter);
				if (arbiter.getUnit().getEnergy() < 150) {
					frontDragoon = BattleManager.changeLeader(frontDragoon, frontDragoonGroup);
					if (frontDragoon != null) {
						CommandUtil.patrolMove(arbiter.getUnit(), frontDragoon.getUnit().getPosition());
					}
				}
				if (iterator.hasNext()) {
					arbiter = arbiterGroup.battleUnits.get(iterator.next());
					this.arbiterRecallAttack(arbiter);
					this.arbiterStatisFieldAttack(arbiter);
					if (arbiter.getUnit().getEnergy() < 150) {
						subDragoon = BattleManager.changeLeader(subDragoon, subDragoonGroup);
						if (subDragoon != null) {
							CommandUtil.patrolMove(arbiter.getUnit(), subDragoon.getUnit().getPosition());
						}
					}
				}
			}
		}
	}
	
	protected void arbiterStatisFieldAttack(BattleUnit arbiter) {
		if (arbiter.getUnit().getEnergy() > 100) {
			for (Unit enemy : arbiter.getUnit().getUnitsInRadius(WeaponType.Stasis_Field.maxRange())) {
				if (enemy.getPlayer() == MyBotModule.Broodwar.enemy() && enemy.exists() && !enemy.isStasised()) {
					if (enemy.getType() == UnitType.Terran_Siege_Tank_Tank_Mode ||
						enemy.getType() == UnitType.Terran_Siege_Tank_Siege_Mode ||
						enemy.getType() == UnitType.Terran_Goliath ||
						enemy.getType() == UnitType.Protoss_Carrier ||
						enemy.getType() == UnitType.Protoss_Dragoon) {
						((Arbiter)arbiter).stasisField(enemy.getPosition());
					}
				}
			}
		} 
	}
	
	protected void arbiterRecallAttack(BattleUnit arbiter) {
		if (MyBotModule.Broodwar.getFrameCount() % 72 != 0) {
			return;
		}
		
		if (arbiter.getUnit().getEnergy() > 150) {
			BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
			if (enemyBaseLocation != null) {
				TilePosition enemyPosition = ProtossBasicBuildPosition.Instance().getEnemyPosition(enemyBaseLocation.getTilePosition());
				if (enemyPosition == null) {
					enemyPosition = enemyBaseLocation.getTilePosition();
				}
				if (!arbiter.getUnit().isMoving()) {
					arbiter.getUnit().move(enemyPosition.toPosition());			
					arbiter.getUnit().move(enemyBaseLocation.getRegion().getCenter(), true);
				}
				if (enemyBaseLocation.getTilePosition().getDistance(arbiter.getUnit().getTilePosition()) < 15 ||
						arbiter.getUnit().isUnderAttack() ||
						(arbiter.getUnit().getShields() + arbiter.getUnit().getHitPoints() < 200)) {
					if (arbiter.getUnit().getRegion().isAccessible()) {
						BattleUnitGroup dragoonGroup = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue());
						BattleUnit leader = dragoonGroup.getLeader();
						if (leader != null) {
							arbiter.getUnit().useTech(TechType.Recall, dragoonGroup.getLeader().getUnit());
						}
					}
				}
			}
		} 
	}
	
	protected void highTemplarAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		
		BattleUnitGroup highTemplarGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_High_Templar);
		if (highTemplarGroup.getUnitCount() > 0) {
			for (int unitId : highTemplarGroup.battleUnits.keySet()) {
				HighTemplar highTemplar = (HighTemplar) highTemplarGroup.battleUnits.get(unitId);
				if (highTemplar.getUnit().exists()) {
					int maxEnemyCount = 0;
					Unit targetEnemy = null;
					for (Unit enemy : highTemplar.getUnit().getUnitsInRadius(WeaponType.Psionic_Storm.maxRange())) {
						if (enemy.getPlayer() == MyBotModule.Broodwar.enemy() && enemy.exists() && !enemy.isUnderStorm()) {
							if (enemy.getType().isWorker() || (enemy.getType().canAttack() &&
									enemy.getType() != UnitType.Protoss_Zealot &&
									enemy.getType() != UnitType.Terran_Firebat &&
									!enemy.getType().isBuilding())) {
								int enemyUnitCount = 0;
								int selfUnitCount = 0;
								for (Unit unit : enemy.getUnitsInRadius(ENEMY_RADIUS)) {
									if (unit.getPlayer() == MyBotModule.Broodwar.enemy()) {
										enemyUnitCount++;
									} else if (unit.getPlayer() == MyBotModule.Broodwar.self()) {
										selfUnitCount++;
									}
								}
								int gapCount = enemyUnitCount - selfUnitCount;
								if (gapCount > 0 && maxEnemyCount <= gapCount) {
									maxEnemyCount= gapCount;
									targetEnemy = enemy;
								}
							}
						}
					}
					if (maxEnemyCount == 0) {
						BattleUnit leader = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
						BattleUnit highTemplarLeader = BattleManager.changeLeader(highTemplarGroup.getLeader(), highTemplarGroup);
						if (highTemplar.getUnitId() == highTemplarLeader.getUnitId()) {
							BaseLocation firstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(MyBotModule.Broodwar.self());
							CommandUtil.move(highTemplarLeader.getUnit(), firstExpansionLocation.getRegion().getCenter().getPoint());
						} else {
							this.unitFollow(highTemplar, leader);
						}
					} else {
						if (highTemplar.getUnit().getEnergy() < 75 && BattleManager.shouldRetreat(highTemplar.getUnit())) {
							CommandUtil.move(highTemplar.getUnit(), ProtossBasicBuildPosition.mapInfo.get(ProtossBasicBuildPosition.START_BASE).toPosition());
						} else if (targetEnemy != null){						
							System.out.println("psionic target enemy count : " + maxEnemyCount);
							highTemplar.psionicStorm(targetEnemy.getPosition());
						}
					}
				}
				if (highTemplar.isSkillUsed() && highTemplar.getUnit().getEnergy() < 50) {
					BattleManager.instance().addArchonCandidate(highTemplar);
				}
			}
			
			while(BattleManager.instance().getArchonCandidatesCount() > 1) {
				HighTemplar highTemplar = BattleManager.instance().removeArchonCandidate();
				HighTemplar targetHighTemplar = BattleManager.instance().removeArchonCandidate();
				highTemplar.archonWarp(targetHighTemplar.getUnit());
			}
		}
	}
	
	protected void archonAttack() {
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}
		BattleUnitGroup archonGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Archon);
		if (archonGroup.getUnitCount() > 0) {
			for (int unitId : archonGroup.battleUnits.keySet()) {
				BattleUnit archon = archonGroup.battleUnits.get(unitId);
				BattleUnit dragoon = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleGroupType.FRONT_GROUP.getValue()).getLeader();
				if (dragoon != null && dragoon.getUnit().exists()) {
					CommandUtil.patrolMove(archon.getUnit(), dragoon.getUnit().getRegion().getCenter());
				}
			}
		}
	}
	
//	protected void dropAttack() {
//	trainWeapon(UnitType.Protoss_Reaver);
//	if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
//		return;
//	}
//	
//	BattleUnitGroup shuttleGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Shuttle);
//	if (shuttleGroup.getUnitCount() > 0) {
//		for (int unitId : shuttleGroup.battleUnits.keySet()) {
//			BattleUnit shuttle = shuttleGroup.battleUnits.get(unitId);
//			if (shuttle.getUnit().getSpaceRemaining() != 0) {
//				for (Unit unit : shuttle.getUnit().getUnitsInRadius(BASE_RADIUS)) {
//					if (unit.getPlayer() == MyBotModule.Broodwar.self()) {
//						if ((unit.getType() == UnitType.Protoss_High_Templar && (unit.isUnderAttack() || unit.getEnergy() < 50)) ||
//							(unit.getType() == UnitType.Protoss_Reaver && (unit.isUnderAttack() || unit.getScarabCount() == 0))) {				
//							System.out.println(unit.getID() + ", hp : " + unit.getShields() + unit.getHitPoints() +", mp : " + unit.getEnergy());
//							shuttle.getUnit().load(unit);
//						} 
//					}
//				}
//			} else {
//				if (!shuttle.getUnit().getPosition().toTilePosition().equals(new TilePosition(60, 120))) {
//					System.out.println("target not reached, current position : " +shuttle.getUnit().getPosition().toTilePosition());
//
//				}
//				System.out.println("shuttle current position : " +shuttle.getUnit().getPosition().toTilePosition());
//				if (shuttle.getUnit().getPosition().toTilePosition().equals(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter().toTilePosition())) {
//					System.out.println("target reached, current position : " + shuttle.getUnit().getPosition().toTilePosition());
//					shuttle.getUnit().unloadAll();
//					CommandUtil.move(shuttle.getUnit(), InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getPosition());
//					for (Unit unit : shuttle.getUnit().getLoadedUnits()) {
//						if (unit.getType() == UnitType.Protoss_High_Templar && 
//								unit.getEnergy() > TechType.Psionic_Storm.energyCost()) {
//							System.out.println(unit.getID() + ", enegy : " + unit.getEnergy());
//							shuttle.getUnit().unload(unit);
//						} else if (unit.getType() == UnitType.Protoss_Reaver &&
//								unit.getScarabCount() > 3) {
//							System.out.println(unit.getID() + ", scrap : " + unit.getScarabCount());
//							shuttle.getUnit().unload(unit);
//						}
//					}
//				}
//				
//			}
//		}
//	}
//}

//protected void corsairAttack() {
//	if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
//		return;
//	}
//	
//	BattleUnitGroup corsairGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Corsair);
//	if (corsairGroup.getUnitCount() > 0) {
//		for (int unitId : corsairGroup.battleUnits.keySet()) {
//			Corsair corsair = (Corsair) corsairGroup.battleUnits.get(unitId);
//			if (corsair.getUnit().isUnderAttack()) {
//				for (Unit enemy : corsair.getUnit().getUnitsInRadius(CORSAIR_RADIUS)) {
//					if (enemy != null && !enemy.isUnderDisruptionWeb() && enemy.getPlayer() == MyBotModule.Broodwar.enemy()) {
//						if (enemy.getType() == UnitType.Protoss_Photon_Cannon ||
//								enemy.getType() == UnitType.Terran_Bunker ||
//								enemy.getType() == UnitType.Terran_Missile_Turret ||
//								enemy.getType() == UnitType.Zerg_Sunken_Colony ||
//								enemy.getType() == UnitType.Zerg_Spore_Colony) {
//							corsair.disruptionWeb(enemy);
//						}
//					}
//				}
//			} else {
//				BattleUnitGroup arbiterGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Arbiter);
//				BattleUnit arbiter = arbiterGroup.getLeader();
//				if (arbiter != null && arbiter.getUnit().exists() && arbiter.getUnit().getEnergy() > 150) {
//					//required move logic
//					boolean isAroundArbiter = false;
//					for (Unit unit : corsair.getUnit().getUnitsInRadius(CORSAIR_RADIUS)) {
//						if (unit.getType() == UnitType.Protoss_Arbiter) {
//							isAroundArbiter = true;
//							break;
//						}
//					}
//					if (!isAroundArbiter) {
//						corsair.getUnit().stop();
//					} else {
//						BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
//						CommandUtil.move(corsair.getUnit(), enemyBaseLocation.getPosition());
//					}
//				}
//			}
//		}
//	}
//}
//
//protected void carrierAttack() {
//	if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
//		return;
//	}
//	
//	trainWeapon(UnitType.Protoss_Carrier);
//}
}
