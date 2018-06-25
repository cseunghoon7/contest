import java.util.Iterator;

import bwapi.Player;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BaseLocation;
import bwta.Chokepoint;

/// 상황을 판단하여, 정찰, 빌드, 공격, 방어 등을 수행하도록 총괄 지휘를 하는 class <br>
/// InformationManager 에 있는 정보들로부터 상황을 판단하고, <br>
/// BuildManager 의 buildQueue에 빌드 (건물 건설 / 유닛 훈련 / 테크 리서치 / 업그레이드) 명령을 입력합니다.<br>
/// 정찰, 빌드, 공격, 방어 등을 수행하는 코드가 들어가는 class
public class StrategyManager {

	private static StrategyManager instance = new StrategyManager();

	/// static singleton 객체를 리턴합니다
	public static StrategyManager Instance() {
		return instance;
	}
	
	private boolean isInitialBuildOrderFinished;

	public StrategyManager() {
		isInitialBuildOrderFinished = false;
	}

	/// 경기가 시작될 때 일회적으로 전략 초기 세팅 관련 로직을 실행합니다
	public void onStart() {
		setInitialBuildOrder();
	}

	///  경기가 종료될 때 일회적으로 전략 결과 정리 관련 로직을 실행합니다
	public void onEnd(boolean isWinner) {
		
	}

	/// 경기 진행 중 매 프레임마다 경기 전략 관련 로직을 실행합니다
	public void update() {
		if (BuildManager.Instance().buildQueue.isEmpty()) {
			isInitialBuildOrderFinished = true;
		}
		executeWorkerTraining();
		executeSupplyManagement();

		executeBuildOrderManagement();
		executeBattleUnitTraining();
		executeBattle();
		executeDefense();
		executeObservatoring();
	}
	
	public void executeBuildOrderManagement() {
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) == 1 && MyBotModule.Broodwar.self().minerals() >= 400) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Nexus) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Nexus, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Nexus, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, true);
			}
		}
		if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) == 2 && MyBotModule.Broodwar.self().minerals() >= 300 && BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Gateway) < 4) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Gateway) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Gateway, null) < 2) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
		if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) == 2 && MyBotModule.Broodwar.self().minerals() >= 300 && BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Gateway) ==4) {
			if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Gateway) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Gateway, null) < 2) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, false);
			}
		}
		
		BuildingUnit cyberneticsCore = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core);
//		if (cyberneticsCore != null && cyberneticsCore.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
//				MyBotModule.Broodwar.self().minerals() >= 150) {
//			if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge) == null && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Forge) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Forge, null) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Forge, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
//			}
//		}
		
//		BuildingUnit forge = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge);
//		if (forge != null && forge.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
//				MyBotModule.Broodwar.self().minerals() >= 150) {
//			if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Photon_Cannon) < 2 && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Photon_Cannon) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Photon_Cannon, null) == 0) {
//				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.FirstChokePoint, true);
//			}
//		}
		
		if (cyberneticsCore != null && cyberneticsCore.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
				MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100) {
			if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Gateway) < 3) {
				if (BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Gateway) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Gateway, null) == 0) {
					BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, true);
				}
			}
			if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) == null && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Citadel_of_Adun) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Citadel_of_Adun, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
		
		BuildingUnit citadelOfAdun = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun);
		if (citadelOfAdun != null && citadelOfAdun.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
				MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().gas() >= 200) {
			if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility) == null && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Robotics_Facility) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Robotics_Facility, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Facility, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
		
		BuildingUnit roboticsFacility = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility);
		if (roboticsFacility != null && roboticsFacility.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
				MyBotModule.Broodwar.self().minerals() >= 50 && MyBotModule.Broodwar.self().gas() >= 100) {
			if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Observatory) == null && BuildManager.Instance().buildQueue.getItemCount(UnitType.Protoss_Observatory) == 0 && ConstructionManager.Instance().getConstructionQueueItemCount(UnitType.Protoss_Observatory, null) == 0) {
				BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observatory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, false);
			}
		}
	}
	
	public void executeBattleUnitTraining() {
		if (isInitialBuildOrderFinished == false) {
			return;
		}
		
		if (MyBotModule.Broodwar.self().supplyUsed() < MyBotModule.Broodwar.self().supplyTotal()) {
			if (MyBotModule.Broodwar.self().minerals() >= 100) {
				if (BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleUnitGroupManager.FRONT_GROUP).getUnitCount() <= 12) {
					BuildingUnitManager.instance().trainBuildingUnit(UnitType.Protoss_Gateway, UnitType.Protoss_Zealot);
				}
			}
			BuildingUnit cyberneticsCore = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core);
			if (cyberneticsCore != null && cyberneticsCore.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED) {
				if (cyberneticsCore.getUnit().canUpgrade(UpgradeType.Singularity_Charge)) {
					cyberneticsCore.getUnit().upgrade(UpgradeType.Singularity_Charge);
					cyberneticsCore.completeUpgrade(UpgradeType.Singularity_Charge);
				}
				if (MyBotModule.Broodwar.self().minerals() >= 125 && MyBotModule.Broodwar.self().gas() >= 50) {
					if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Gateway) > 2) {
						BuildingUnitManager.instance().trainBuildingUnit(UnitType.Protoss_Gateway, UnitType.Protoss_Dragoon);
					}
				}
			}
			BuildingUnit citadelOfAdun = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun);
			if (citadelOfAdun != null && citadelOfAdun.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED) {
				if (citadelOfAdun.getUnit().canUpgrade(UpgradeType.Leg_Enhancements)) {
					citadelOfAdun.getUnit().upgrade(UpgradeType.Leg_Enhancements);
					citadelOfAdun.completeUpgrade(UpgradeType.Leg_Enhancements);
				}
			}
			BuildingUnit observatory = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Observatory);
			if (observatory != null && observatory.getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED) {
				if (MyBotModule.Broodwar.self().minerals() >= 25 && MyBotModule.Broodwar.self().gas() >= 75) {
					BuildingUnit roboticsFacility = BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility);
					if (!roboticsFacility.getUnit().isTraining() && BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Observer).getUnitCount() < 2) {
						roboticsFacility.getUnit().train(UnitType.Protoss_Observer);
					}
				}
			}
		}
	}
	
	public void executeBattle() {
		int zealotCount = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleUnitGroupManager.FRONT_GROUP).getUnitCount();
		int dragoonCount = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Dragoon).get(BattleUnitGroupManager.FRONT_GROUP).getUnitCount();
		if (zealotCount >= 12 && dragoonCount <= 10) {
			Chokepoint enemyFirstChokePoint = InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.enemy());
			BattleManager.instance().totalAttack(enemyFirstChokePoint.getCenter());
		}
		if (dragoonCount > 10) {
			BaseLocation enemyBaseLocation = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.enemy());
			BattleManager.instance().totalAttack(enemyBaseLocation.getPosition());
		}
	}
	
	public void executeDefense() {
		for (Unit unit : MyBotModule.Broodwar.enemy().getUnits()) {
			if (InformationManager.Instance().selfPlayer.getStartLocation().getDistance(unit.getPosition().toTilePosition()) < 50) {
				BattleManager.instance().closestAttack();
			}
		}
	}
	
	public void executeObservatoring() {
		Unit zealot = BattleUnitGroupManager.instance().getBattleUnitGroups(UnitType.Protoss_Zealot).get(BattleUnitGroupManager.FRONT_GROUP).getLeader();
		if (zealot != null) {
			BattleUnitGroup observerGroup = BattleUnitGroupManager.instance().getBattleUnitGroup(UnitType.Protoss_Observer);
			if (!observerGroup.battleUnits.isEmpty()) {
				Iterator<Integer> iterator = observerGroup.battleUnits.keySet().iterator();
				while (iterator.hasNext()) {
					int unitId = iterator.next();
					BattleUnit observer = observerGroup.battleUnits.get(unitId);
					if (observer != null) {
						if (!observer.getUnit().isFollowing() && observer.getUnit().canFollow(zealot)) {
							observer.getUnit().follow(zealot);
						}
					}
				}
			}
		}
		
	}

	public void setInitialBuildOrder() {
		if (MyBotModule.Broodwar.self().getRace() == Race.Protoss) {
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Pylon,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Assimilator,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Probe,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Cybernetics_Core,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Singularity_Charge);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Leg_Enhancements);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Templar_Archives);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_High_Templar);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Psionic_Storm);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Facility);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observatory);
//			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observer);
			
			/*
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Assimilator,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Forge,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Photon_Cannon,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Gateway,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Zealot);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Cybernetics_Core,
					BuildOrderItem.SeedPositionStrategy.MainBaseLocation);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dragoon);
			// 드라군 사정거리 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Singularity_Charge);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Citadel_of_Adun);
			// 질럿 속도 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Leg_Enhancements);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Shield_Battery);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Templar_Archives);
			// 하이템플러
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_High_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_High_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Psionic_Storm);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Hallucination);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Khaydarin_Amulet);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Archon);

			// 다크아칸
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Templar);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Maelstrom);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Mind_Control);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Argus_Talisman);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Dark_Archon);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Facility);

			// 셔틀
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Shuttle);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Robotics_Support_Bay);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Drive);

			// 리버
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Reaver);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Scarab_Damage);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Reaver_Capacity);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Scarab);

			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observatory);
			// 옵저버
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Observer);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Boosters);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Sensor_Array);

			// 공중유닛
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Stargate);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Fleet_Beacon);

			// 스카우트
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Scout);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Apial_Sensors);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Gravitic_Thrusters);

			// 커세어
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Corsair);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Disruption_Web);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Argus_Jewel);

			// 캐리어
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Carrier);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Carrier_Capacity);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Interceptor);

			// 아비터
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Arbiter_Tribunal);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Protoss_Arbiter);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Recall);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(TechType.Stasis_Field);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Khaydarin_Core);

			// 포지 - 지상 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Ground_Weapons);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Plasma_Shields);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Ground_Armor);

			// 사이버네틱스코어 - 공중 유닛 업그레이드
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Air_Weapons);
			BuildManager.Instance().buildQueue.queueAsLowestPriority(UpgradeType.Protoss_Air_Armor);

			*/
		}
	}

	// 일꾼 계속 추가 생산
	public void executeWorkerTraining() {
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType().isResourceDepot()) {
					if (unit.isTraining()) {
						workerCount += unit.getTrainingQueue().size();
					}
				}
			}

			if (workerCount < 30) {
				BuildingUnitManager.instance().trainBuildingUnit(UnitType.Protoss_Nexus, UnitType.Protoss_Probe);
			}
		}
	}

	// Supply DeadLock 예방 및 SupplyProvider 가 부족해질 상황 에 대한 선제적 대응으로서<br>
	// SupplyProvider를 추가 건설/생산한다
	public void executeSupplyManagement() {

		// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
		// 가이드 추가 및 콘솔 출력 명령 주석 처리

		// InitialBuildOrder 진행중 혹은 그후라도 서플라이 건물이 파괴되어 데드락이 발생할 수 있는데, 이 상황에 대한 해결은 참가자께서 해주셔야 합니다.
		// 오버로드가 학살당하거나, 서플라이 건물이 집중 파괴되는 상황에 대해  무조건적으로 서플라이 빌드 추가를 실행하기 보다 먼저 전략적 대책 판단이 필요할 것입니다

		// BWAPI::Broodwar->self()->supplyUsed() > BWAPI::Broodwar->self()->supplyTotal()  인 상황이거나
		// BWAPI::Broodwar->self()->supplyUsed() + 빌드매니저 최상단 훈련 대상 유닛의 unit->getType().supplyRequired() > BWAPI::Broodwar->self()->supplyTotal() 인 경우
		// 서플라이 추가를 하지 않으면 더이상 유닛 훈련이 안되기 때문에 deadlock 상황이라고 볼 수도 있습니다.
		// 저그 종족의 경우 일꾼을 건물로 Morph 시킬 수 있기 때문에 고의적으로 이런 상황을 만들기도 하고, 
		// 전투에 의해 유닛이 많이 죽을 것으로 예상되는 상황에서는 고의적으로 서플라이 추가를 하지 않을수도 있기 때문에
		// 참가자께서 잘 판단하셔서 개발하시기 바랍니다.
		
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (isInitialBuildOrderFinished == false) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
			// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
			int supplyMargin = 12;

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {
				
				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;
				onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(
						InformationManager.Instance().getBasicSupplyProviderUnitType(), null)
						* InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();

				if (currentSupplyShortage > onBuildingSupplyCount) {
					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() 
							&& currentItem.metaType.getUnitType() == UnitType.Protoss_Pylon) 
						{
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						// 주석처리
						BuildManager.Instance().buildQueue.queueAsHighestPriority(UnitType.Protoss_Pylon, true);
					}
				}
			}
		}

		// BasicBot 1.1 Patch End ////////////////////////////////////////////////		
	}
}