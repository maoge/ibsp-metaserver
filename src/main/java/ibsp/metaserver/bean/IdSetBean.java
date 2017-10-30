package ibsp.metaserver.bean;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class IdSetBean<E> implements Iterable<E> {
	
	private Set<E> idSet;

	public IdSetBean() {
		newSet();
	}

	private void newSet() {
		idSet = new HashSet<E>();
	}

	public void addId(E id) {
		if (idSet == null) {
			newSet();
		}

		idSet.add(id);
	}

	public boolean isIdExist(E id) {
		if (idSet == null)
			return false;

		return idSet.contains(id);
	}
	
	public boolean removeId(E id) {
		boolean ret = false;
		if (isIdExist(id)) {
			ret = idSet.remove(id);
		}
		
		return ret;
	}
	
	public boolean isEmpty() {
		return idSet == null ? true : idSet.isEmpty();
	}
	
	public int size() {
		return idSet == null ? 0 : idSet.size();
	}
	
	public void clear() {
		if (idSet != null) {
			idSet.clear();
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new Iter(idSet.iterator());
	}
	
	private class Iter implements Iterator<E> {

		final Iterator<E> listIter;

		Iter(Iterator<E> listIter) {
			this.listIter = listIter;
		}

		@Override
		public boolean hasNext() {
			return listIter.hasNext();
		}

		@Override
		public E next() {
			E val = listIter.next();
			return val;
		}

		@Override
		public void remove() {
			listIter.remove();
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("\"");
		Iterator<E> iter = this.iterator();
		int idx = 0;
		while(iter.hasNext()) {
			if (idx++ > 0)
				sb.append(",");
			
			Object obj = iter.next();
			sb.append(obj.toString());
		}
		sb.append("\"");
		
		return sb.toString();
	}
	
	public String concat() {
		StringBuffer sb = new StringBuffer("");
		
		Iterator<E> iter = this.iterator();
		int idx = 0;
		while(iter.hasNext()) {
			if (idx++ > 0)
				sb.append(",");
			
			Object obj = iter.next();
			sb.append(obj.toString());
		}
		
		return sb.toString();
	}
	
	public boolean contains(String id) {
		return idSet.contains(id);
	}

}
