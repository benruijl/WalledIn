import walledin.game.entity.*

[
            (Family.PLAYER): { entity ->
                //entity.addBehavior(new PhysicsBehavior(entity));
            } as EntityFunction,
            
            (Family.FOAMGUN_BULLET): { entity ->
                //entity.addBehavior(new PhysicsBehavior(entity, 0.5, false, false));
            } as EntityFunction,
            
            (Family.HANDGUN_BULLET): { entity ->
                //entity.addBehavior(new PhysicsBehavior(entity, 0.5, false, false));
            } as EntityFunction,  
            
            (Family.ITEM): { entity ->
                //entity.addBehavior(new PhysicsBehavior(entity, 10)); // every item weighs the same
            } as EntityFunction,
        ]