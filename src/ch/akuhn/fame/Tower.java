package ch.akuhn.fame;

/**
 * 
 * 
 * @author Adrian Kuhn
 *
 */
public class Tower {

    public final Repository model;
    
    public final MetaRepository metamodel;
    
    public final MetaRepository metaMetamodel;
    
    private Tower(MetaRepository m3, MetaRepository m2, Repository m1) {
        this.metaMetamodel = m3;
        this.metamodel = (m2 != null) ? m2 : new MetaRepository(metaMetamodel);
        this.model = (m1 != null) ? m1 : new Repository(metamodel);
        assert metaMetamodel.getMetamodel().equals(metaMetamodel);
        assert metamodel.getMetamodel().equals(metaMetamodel);
        assert model.getMetamodel().equals(metamodel);
    }
    
    public Tower() {
        this(MetaRepository.createFM3(), null, null);
    }
    
    public MetaRepository getMetaMetamodel() {
        return metaMetamodel;
    }

    public MetaRepository getMetamodel() {
        return metamodel;
    }

    public Repository getModel() {
        return model;
    }
    
}
