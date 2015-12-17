package fr.ifremer.sensornanny.sync.writer.impl;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dao.IElasticDao;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.writer.IElasticWriter;

public class ElasticWriterImpl implements IElasticWriter {

    @Inject
    IElasticDao elasticDao;

    @Override
    public void delete(String uuid) {
        elasticDao.delete(uuid);
    }

    @Override
    public boolean write(String uuid, ObservationJson observation) {
        return elasticDao.write(uuid, observation);
    }

}
