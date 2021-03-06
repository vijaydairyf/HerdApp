package com.marmar.farmapp.hibernate;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import com.marmar.farmapp.config.Configuration;
import com.marmar.farmapp.model.Animal;
import com.marmar.farmapp.model.Livestock;
import com.marmar.farmapp.model.LivestockEvent;
import com.marmar.farmapp.model.Race;

@Repository
public class LivestockDAO {

	@Autowired
	HibernateConector csf;

	public LivestockDAO(HibernateConector csf) {
		this.csf = csf;
	}

	public Object saveNew(Livestock o) {
		return csf.saveObject(o);
	}

	public void update(Livestock r) {
		csf.updateObject(r);
	}

	public void delete(LivestockEvent r) {
		csf.deleteObject(r);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getAll() {
		String hql = "From Livestock";
		return (ArrayList<Livestock>) csf.executeHQLQuery(hql);
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getLivestockFiltred(
			int sexo, int activo, int arete, 
			Animal animal, int edad,int meses,
			boolean sortFN) {
		/*
		 * Activo:
		 * 	  Todo           - 0
		 *    Solo Activo    - 1
		 *    Solo Archivados- 2
		 *    
		 * Arete:
		 * 	  Ambos           -0
		 * 	  Registrados     -1
		 *    Sin Registrar   -2
		 *    
		 * Sexo:
		 * 	  Ambos           -0
		 *    Machos          -1
		 *    Hembras         -2
		 *    
		 * Edades:
		 * 	  Todas           -0
		 *    Mayor           -1
		 *    Menor           -2
		 */

		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			Criteria cr = session.createCriteria(Livestock.class);

			// filter the animal type
			if (animal != null) {
				ApplicationContext ctx = Configuration.getInstance().getApplicationContext();
				RaceDAO r = ctx.getBean(RaceDAO.class);
				Collection<Race> values = r.getRaces(animal);
				Criterion c1 = Restrictions.in("id_race_1", values);
				cr.add(c1);
			}

			// filtrar el sexo
			if (sexo == 0) {
			} else {
				Criterion c = Restrictions.like("gender", sexo - 1);
				cr.add(c);
			}

			// filtrar el estado de actividad del animal
			if (activo==0) {
				// filter nothing
			} else if (activo==1) {
				Criterion c = Restrictions.like("active", true);
				cr.add(c);
			} else {
				Criterion c = Restrictions.like("active", false);
				cr.add(c);
			}

			// filtro para los aretes
			if (arete==0) {
			} else if (arete==1) {
				Criterion c = Restrictions.isNotNull("ear_ring");
				cr.add(c);
			} else {
				Criterion c = Restrictions.isNull("ear_ring");
				cr.add(c);
			}

			if(sortFN) {
				//order by date
				cr.addOrder(Order.asc("date_birth"));
			}
			
			result = (ArrayList<Livestock>) cr.list();

			tx.commit();

		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}

		if (result != null) {
			ArrayList<Livestock> filtrados = new ArrayList<>(result.size());
			if (edad==0) {
				return result;
			} else if (edad==1) {

				for (Livestock b : result) {
					if (b.getAge() >= meses) {
						filtrados.add(b);
					}
				}
			} else {
				for (Livestock b : result) {
					if (b.getAge() <= meses) {
						filtrados.add(b);
					}
				}

			}
			return filtrados;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<LivestockEvent> getLivestockAtEvents(Livestock livestock) {

		ArrayList<LivestockEvent> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();

			String hql = "From LivestockEvent where id_livestock like '" + livestock.getId_livestock() + "'";
			Query query = session.createQuery(hql);
			result = (ArrayList<LivestockEvent>) query.list();

			tx.commit();

		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getMothersOffspring(Livestock livestock) {
		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			String hql = "From Livestock where id_mother like '" + livestock.getId_livestock() + "'";
			Query query = session.createQuery(hql);
			result = (ArrayList<Livestock>) query.list();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getFathersOffspring(Livestock livestock) {
		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			String hql = "From Livestock where id_father like '" + livestock.getId_livestock() + "'";
			Query query = session.createQuery(hql);
			result = (ArrayList<Livestock>) query.list();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getLivestockGenderAnimal(int sexo, Animal animal) {
		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria cr = session.createCriteria(Livestock.class);

			// filter the animal type
			ApplicationContext ctx = Configuration.getInstance().getApplicationContext();
			RaceDAO raceses = ctx.getBean(RaceDAO.class);
			Collection<Race> values = raceses.getRaces(animal);
			Criterion c1 = Restrictions.in("id_race_1", values);
			cr.add(c1);

			// filter only the active ones
			Criterion c2 = Restrictions.like("active", true);
			cr.add(c2);

			// filtrar el sexo
			if (sexo == 0) {
			} else {
				Criterion c = Restrictions.like("gender", sexo - 1);
				cr.add(c);
			}
			
			//order by date
			cr.addOrder(Order.asc("date_birth"));
			
			result = (ArrayList<Livestock>) cr.list();
			tx.commit();
		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getAllDeadLivestock() {

		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria cr = session.createCriteria(Livestock.class);

			// filtro para las muertes
			Criterion c = Restrictions.isNotNull("date_death");
			cr.add(c);
			
			//order by date
			cr.addOrder(Order.asc("date_birth"));

			result = (ArrayList<Livestock>) cr.list();
			tx.commit();

		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Livestock> getDeadLivestockOfAnimal(Animal animal) {

		ArrayList<Livestock> result = null;
		Session session = csf.getSf().openSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			Criteria cr = session.createCriteria(Livestock.class);

			// filter the animal type
			ApplicationContext ctx = Configuration.getInstance().getApplicationContext();
			RaceDAO raceses = ctx.getBean(RaceDAO.class);
			Collection<Race> values = raceses.getRaces(animal);
			Criterion c1 = Restrictions.in("id_race_1", values);
			cr.add(c1);

			// filtro para las muertes
			Criterion c = Restrictions.isNotNull("date_death");
			cr.add(c);
			
			//order by date
			cr.addOrder(Order.asc("date_birth"));

			result = (ArrayList<Livestock>) cr.list();
			tx.commit();

		} catch (HibernateException he) {
			if (tx != null) {
				tx.rollback();
			}
			System.err.println(he.getMessage());
			he.printStackTrace();
		} finally {
			session.close();
		}

		return result;
	}
}
