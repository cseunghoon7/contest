import bwapi.TilePosition;
import bwapi.UnitType;

public class ZergBasicBuildingUnitOrder extends BuildingUnitOrder {

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		super.order(UnitType.Protoss_Assimilator, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Assimilator) == 0 && 
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core) != null &&
						MyBotModule.Broodwar.self().minerals() >= 100) {
					return true;
				}
				return false;
			}
		});
		
		TilePosition thirdPhoton = new TilePosition(ProtossBasicBuildPosition.thirdPhotonPosX, ProtossBasicBuildPosition.thirdPhotonPosY);
		super.order(UnitType.Protoss_Photon_Cannon, thirdPhoton, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Photon_Cannon) < 3 &&
						MyBotModule.Broodwar.self().minerals() >= 150) {
					return true;
				}
				return false;
			}
		});

		super.order(UnitType.Protoss_Photon_Cannon, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Forge).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Photon_Cannon) >= 3 &&
								BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Photon_Cannon) < 5 &&
						MyBotModule.Broodwar.self().minerals() >= 150) {
					return true;
				}
				return false;
			}
		});
		
		
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
		
		super.order(UnitType.Protoss_Cybernetics_Core, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnitCount(UnitType.Protoss_Pylon) > 1 &&
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
						BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Gateway) > 3 &&
						MyBotModule.Broodwar.self().minerals() >= 400) {
					return true;
				}
				return false;
			}
		});
		
		super.orderExpansionDefence();
		
		super.order(UnitType.Protoss_Citadel_of_Adun, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getCompletedBuildingUnitCount(UnitType.Protoss_Nexus) >= 2 &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Cybernetics_Core).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) == null &&
						MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 100) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Robotics_Facility, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Robotics_Facility) == null &&
						MyBotModule.Broodwar.self().minerals() >= 200 && MyBotModule.Broodwar.self().gas() >= 200) {
					return true;
				}
				return false;
			}
		});
		
		super.order(UnitType.Protoss_Observatory, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
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
		
		super.orderCenterExpansion();
		
		super.orderStartBaseExpansion();
		
		super.orderPylonGateways();
		
		super.order(UnitType.Protoss_Templar_Archives, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, new OrderCondition() {
			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				if (BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Citadel_of_Adun).getBuildingStatus() == BuildingUnit.BuildingStatus.COMPLETED &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Observatory) != null &&
						BuildingUnitManager.instance().getBuildingUnit(UnitType.Protoss_Templar_Archives) == null &&
						MyBotModule.Broodwar.self().minerals() >= 150 && MyBotModule.Broodwar.self().gas() >= 200) {
					return true;
				}
				return false;
			}
		});
	}
}
