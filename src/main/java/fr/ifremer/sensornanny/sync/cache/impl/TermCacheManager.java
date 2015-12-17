package fr.ifremer.sensornanny.sync.cache.impl;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.cache.AbstractCacheManager;
import fr.ifremer.sensornanny.sync.dao.ITermDao;
import fr.ifremer.sensornanny.sync.dto.model.Term;

/**
 * Concrete implementation of cache manager for Term
 * 
 * @author athorel
 *
 */
public class TermCacheManager extends AbstractCacheManager<String, Term> {

    @Inject
    private ITermDao termDao;

    @Override
    protected Term read(String key) {
        return termDao.getTerm(key);
    }

}
