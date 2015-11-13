package ch.akuhn.fame.test;

import java.util.Collection;

import ch.akuhn.fame.FameDescription;
import ch.akuhn.fame.FamePackage;
import ch.akuhn.fame.FameProperty;
import ch.akuhn.fame.internal.MultivalueSet;

@FamePackage("RPG")
public class DungeonExample {

    @FameDescription("Dragon")
    public static class Dragon {

        private Collection<Treasure> hoard;

        @FameProperty(name = "hoard", opposite = "keeper")
        public Collection<Treasure> getHoard() {
            if (hoard == null) {
                hoard = new MultivalueSet<Treasure>() {
                    @Override
                    protected void clearOpposite(Treasure e) {
                        e.setKeeper(null);
                    }

                    @Override
                    protected void setOpposite(Treasure e) {
                        e.setKeeper(Dragon.this);
                    }
                };
            }
            return hoard;
        }

        public void setHoard(Collection<? extends Treasure> hoard) {
            this.getHoard().clear();
            this.getHoard().addAll(hoard);
        }

        public void addHoard(Treasure one) {
            this.getHoard().add(one);
        }

        public void addHoard(Treasure one, Treasure... many) {
            this.getHoard().add(one);
            for (Treasure each: many)
                this.getHoard().add(each);
        }

        public void addHoard(Iterable<? extends Treasure> many) {
            for (Treasure each: many)
                this.getHoard().add(each);
        }

        public void addHoard(Treasure[] many) {
            for (Treasure each: many)
                this.getHoard().add(each);
        }

        public int numberOfHoard() {
            return getHoard().size();
        }

        public boolean hasHoard() {
            return !getHoard().isEmpty();
        }

        private Collection<Hero> killedBy;

        @FameProperty(name = "killedBy", opposite = "kills")
        public Collection<Hero> getKilledBy() {
            if (killedBy == null) {
                killedBy = new MultivalueSet<Hero>() {
                    @Override
                    protected void clearOpposite(Hero e) {
                        e.getKills().remove(Dragon.this);
                    }

                    @Override
                    protected void setOpposite(Hero e) {
                        e.getKills().add(Dragon.this);
                    }
                };
            }
            return killedBy;
        }

        public void setKilledBy(Collection<? extends Hero> killedBy) {
            this.getKilledBy().clear();
            this.getKilledBy().addAll(killedBy);
        }

        public void addKilledBy(Hero one) {
            this.getKilledBy().add(one);
        }

        public void addKilledBy(Hero one, Hero... many) {
            this.getKilledBy().add(one);
            for (Hero each: many)
                this.getKilledBy().add(each);
        }

        public void addKilledBy(Iterable<? extends Hero> many) {
            for (Hero each: many)
                this.getKilledBy().add(each);
        }

        public void addKilledBy(Hero[] many) {
            for (Hero each: many)
                this.getKilledBy().add(each);
        }

        public int numberOfKilledBy() {
            return getKilledBy().size();
        }

        public boolean hasKilledBy() {
            return !getKilledBy().isEmpty();
        }

    }

    @FamePackage("RPG")
    @FameDescription("Hero")
    public static class Hero {

        private Treasure talisman;

        @FameProperty(name = "talisman", opposite = "owner")
        public Treasure getTalisman() {
            return talisman;
        }

        public void setTalisman(Treasure talisman) {
            if (this.talisman == null ? talisman != null : !this.talisman.equals(talisman)) {
                Treasure old_talisman = this.talisman;
                this.talisman = talisman;
                if (old_talisman != null) old_talisman.setOwner(null);
                if (talisman != null) talisman.setOwner(this);
            }
        }

        private Hero twin;

        @FameProperty(name = "twin", opposite = "twin")
        public Hero getTwin() {
            return twin;
        }

        public void setTwin(Hero twin) {
            if (this.twin == null ? twin != null : !this.twin.equals(twin)) {
                Hero old_twin = this.twin;
                this.twin = twin;
                if (old_twin != null) old_twin.setTwin(null);
                if (twin != null) twin.setTwin(this);
            }
        }

        private Collection<Dragon> kills;

        @FameProperty(name = "kills", opposite = "killedBy")
        public Collection<Dragon> getKills() {
            if (kills == null) {
                kills = new MultivalueSet<Dragon>() {
                    @Override
                    protected void clearOpposite(Dragon e) {
                        e.getKilledBy().remove(Hero.this);
                    }

                    @Override
                    protected void setOpposite(Dragon e) {
                        e.getKilledBy().add(Hero.this);
                    }
                };
            }
            return kills;
        }

        public void setKills(Collection<? extends Dragon> kills) {
            this.getKills().clear();
            this.getKills().addAll(kills);
        }

        public void addKills(Dragon one) {
            this.getKills().add(one);
        }

        public void addKills(Dragon one, Dragon... many) {
            this.getKills().add(one);
            for (Dragon each: many)
                this.getKills().add(each);
        }

        public void addKills(Iterable<? extends Dragon> many) {
            for (Dragon each: many)
                this.getKills().add(each);
        }

        public void addKills(Dragon[] many) {
            for (Dragon each: many)
                this.getKills().add(each);
        }

        public int numberOfKills() {
            return getKills().size();
        }

        public boolean hasKills() {
            return !getKills().isEmpty();
        }

    }

    @FamePackage("RPG")
    @FameDescription("Treasure")
    public static class Treasure {

        private Hero owner;

        @FameProperty(name = "owner", opposite = "talisman")
        public Hero getOwner() {
            return owner;
        }

        public void setOwner(Hero owner) {
            if (this.owner == null ? owner != null : !this.owner.equals(owner)) {
                Hero old_owner = this.owner;
                this.owner = owner;
                if (old_owner != null) old_owner.setTalisman(null);
                if (owner != null) owner.setTalisman(this);
            }
        }

        private Dragon keeper;

        @FameProperty(name = "keeper", opposite = "hoard")
        public Dragon getKeeper() {
            return keeper;
        }

        public void setKeeper(Dragon keeper) {
            if (this.keeper != null) {
                if (this.keeper.equals(keeper)) return;
                this.keeper.getHoard().remove(this);
            }
            this.keeper = keeper;
            if (keeper == null) return;
            keeper.getHoard().add(this);
        }

    }

}
