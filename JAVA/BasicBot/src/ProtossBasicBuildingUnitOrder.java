import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bwapi.Region;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class ProtossBasicBuildingUnitOrder extends BuildingUnitOrder {

	@Override
	public void execute() {		
		// TODO Auto-generated method stub
		super.order(UnitType.Protoss_Nexus, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) == 1 && 
						MyBotModule.Broodwar.self().minerals() >= 400) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.SecondChokePoint, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Photon_Cannon) < 4 &&
						MyBotModule.Broodwar.self().minerals() >= 150) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Cybernetics_Core, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) >= 2 &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Assimilator) >= 1 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core) == null &&
						MyBotModule.Broodwar.self().minerals() >= 200) {
					return true;
				}
				return false;
			}
		});
			
		super.order(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) >= 2 &&						
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Gateway) < 8 &&
						MyBotModule.Broodwar.self().minerals() >= 150) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Gateway, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) >= 3 &&						
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Gateway) > 7 &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Gateway) < 9 &&
						MyBotModule.Broodwar.self().minerals() >= 150) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Nexus, BuildOrderItem.SeedPositionStrategy.SecondExpansionLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) == 2 && 
						BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Gateway) > 5 &&
						MyBotModule.Broodwar.self().minerals() >= 400) {
					return true;
				}
				return false;
			}
		});
		
		this.orderExpansionDefence();
			
		super.order(UnitType.Protoss_Citadel_of_Adun, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) == 2 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) == null &&
						MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Robotics_Facility, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) == 2 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility) == null &&
						MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().gas() >= 200) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Observatory, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Observatory) == null &&
						MyBotModule.Broodwar.self().minerals() >= 50 && MyBotModule.Broodwar.self().gas() >= 100) {
					return true;
				}
				return false;
			}
		});
		
		this.orderCenterExpansion();
		
		this.orderPylonGateways();
		
		super.order(UnitType.Protoss_Templar_Archives, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) >= 3 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Templar_Archives) == null &&
						MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 200) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Stargate, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) >= 3 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Stargate) < 1 &&
						MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 150) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Arbiter_Tribunal, BuildOrderItem.SeedPositionStrategy.MainBaseBackYard, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Templar_Archives) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Templar_Archives).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Arbiter_Tribunal) == null &&
						MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().gas() >= 150) {
					return true;
				}
				return false;
			}
		});
	
		
//		super.order(UnitType.Protoss_Robotics_Support_Bay, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
//			@Override
//			public boolean isActive() {
//				// TODO Auto-generated method stub
//				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility) != null &&
//						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
//						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Support_Bay) == null &&
//						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Nexus) == 2 &&
//						MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().gas() >= 200) {
//					return true;
//				}
//				return false;
//			}
//		});
		
//		super.order(UnitType.Protoss_Fleet_Beacon, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
//			@Override
//			public boolean isActive() {
//				// TODO Auto-generated method stub
//				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Stargate) > 0 &&
//						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Fleet_Beacon) == null &&
//						MyBotModule.Broodwar.self().minerals() >= 300 && MyBotModule.Broodwar.self().gas() >= 200) {
//					return true;
//				}
//				return false;
//			}
//		});
	}
}
