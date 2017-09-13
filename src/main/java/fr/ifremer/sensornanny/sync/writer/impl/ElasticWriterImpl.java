package fr.ifremer.sensornanny.sync.writer.impl;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.sync.dao.IObservationDao;
import fr.ifremer.sensornanny.sync.dao.ISystemDao;
import fr.ifremer.sensornanny.sync.dto.elasticsearch.ObservationJson;
import fr.ifremer.sensornanny.sync.dto.model.SensorML;
import fr.ifremer.sensornanny.sync.dto.owncloud.OwncloudSyncModel;
import fr.ifremer.sensornanny.sync.writer.IElasticWriter;

public class ElasticWriterImpl implements IElasticWriter {

    @Inject
    IObservationDao observationDao;

    @Inject
    ISystemDao systemDao;

    @Override
    public void delete(String uuid) {
        observationDao.delete(uuid);
        systemDao.delete(uuid);
    }

    @Override
    public boolean write(String uuid, ObservationJson observation) {
        return observationDao.write(uuid, observation);
    }

    @Override
    public boolean write(String uuid, SensorML system, boolean hasData) {
        return systemDao.write(uuid, system, hasData);
    }

    @Override
    public void flush() {
        observationDao.flush();
        systemDao.flush();
    }
}
