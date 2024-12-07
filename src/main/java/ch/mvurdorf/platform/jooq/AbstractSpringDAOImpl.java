/*
 * This file is generated by jOOQ.
 */
package ch.mvurdorf.platform.jooq;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DAOImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring specific {@link DAOImpl} override.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes", "this-escape"})
@Transactional(readOnly = true)
public abstract class AbstractSpringDAOImpl<R extends UpdatableRecord<R>, P, T> extends DAOImpl<R, P, T> {

    protected AbstractSpringDAOImpl(Table<R> table, Class<P> type) {
        super(table, type);
    }

    protected AbstractSpringDAOImpl(Table<R> table, Class<P> type, Configuration configuration) {
        super(table, type, configuration);
    }

    @Transactional(readOnly = true)
    @Override
    public long count() {
        return super.count();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean exists(P object) {
        return super.exists(object);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsById(T id) {
        return super.existsById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public <Z> List<P> fetch(Field<Z> field, Collection<? extends Z> values) {
        return super.fetch(field, values);
    }

    @Transactional(readOnly = true)
    @Override
    public <Z> List<P> fetch(Field<Z> field, Z... values) {
        return super.fetch(field, values);
    }

    @Transactional(readOnly = true)
    @Override
    public <Z> P fetchOne(Field<Z> field, Z value) {
        return super.fetchOne(field, value);
    }

    @Transactional(readOnly = true)
    @Override
    public <Z> Optional<P> fetchOptional(Field<Z> field, Z value) {
        return super.fetchOptional(field, value);
    }

    @Transactional(readOnly = true)
    @Override
    public <Z> List<P> fetchRange(Field<Z> field, Z lowerInclusive, Z upperInclusive) {
        return super.fetchRange(field, lowerInclusive, upperInclusive);
    }

    @Transactional(readOnly = true)
    @Override
    public List<P> findAll() {
        return super.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public P findById(T id) {
        return super.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<P> findOptionalById(T id) {
        return super.findOptionalById(id);
    }

    @Transactional
    @Override
    public void insert(Collection<P> objs) {
        super.insert(objs);
    }

    @Transactional
    @Override
    public void insert(P obj) {
        super.insert(obj);
    }

    @Transactional
    @Override
    public void insert(P... objs) {
        super.insert(objs);
    }

    @Transactional
    @Override
    public void update(Collection<P> objs) {
        super.update(objs);
    }

    @Transactional
    @Override
    public void update(P obj) {
        super.update(obj);
    }

    @Transactional
    @Override
    public void update(P... objs) {
        super.update(objs);
    }

    @Transactional
    @Override
    public void merge(Collection<P> objs) {
        super.merge(objs);
    }

    @Transactional
    @Override
    public void merge(P obj) {
        super.merge(obj);
    }

    @Transactional
    @Override
    public void merge(P... objs) {
        super.merge(objs);
    }

    @Transactional
    @Override
    public void delete(Collection<P> objs) {
        super.delete(objs);
    }

    @Transactional
    @Override
    public void delete(P obj) {
        super.delete(obj);
    }

    @Transactional
    @Override
    public void delete(P... objs) {
        super.delete(objs);
    }

    @Transactional
    @Override
    public void deleteById(Collection<T> ids) {
        super.deleteById(ids);
    }

    @Transactional
    @Override
    public void deleteById(T id) {
        super.deleteById(id);
    }

    @Transactional
    @Override
    public void deleteById(T... ids) {
        super.deleteById(ids);
    }
}
