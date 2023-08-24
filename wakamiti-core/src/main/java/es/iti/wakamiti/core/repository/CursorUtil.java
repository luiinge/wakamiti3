package es.iti.wakamiti.core.repository;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import org.dizitart.no2.objects.Cursor;


public class CursorUtil {

	public static <T> Stream<T> stream(Cursor<T> cursor) {
		return StreamSupport.stream(spliterator(cursor), false);
	}

	private static <T> Spliterator<T> spliterator(Cursor<T> cursor) {
		return new CursorSpliterator<>(cursor, 0);
	}

	public static class CursorSpliterator<T> implements Spliterator<T> {
		static final int BATCH_UNIT = 1 << 10;  // batch array size increment
		static final int MAX_BATCH = 1 << 25;  // max batch array size;
		private final Cursor<T> cursor;
		private Iterator<T> it;
		private final int characteristics;
		private long est;
		private int batch;

		public CursorSpliterator(Cursor<T> cursor, int characteristics) {
			this.cursor = cursor;
			this.it = null;
			this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
				? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
				: characteristics;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (action == null) throw new NullPointerException();
			if (it == null) {
				it = cursor.iterator();
				est = cursor.size();
			}
			if (it.hasNext()) {
				action.accept(it.next());
				return true;
			}
			return false;
		}

		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			if (action == null) throw new NullPointerException();
			Iterator<? extends T> i;
			if ((i = it) == null) {
				i = it = cursor.iterator();
				est = cursor.size();
			}
			i.forEachRemaining(action);
		}

		@Override
		public Spliterator<T> trySplit() {
			Iterator<? extends T> i;
			long s;
			if ((i = it) == null) {
				i = it = cursor.iterator();
				s = est = cursor.size();
			}
			else
				s = est;
			if (s > 1 && i.hasNext()) {
				int n = batch + BATCH_UNIT;
				if (n > s)
					n = (int) s;
				if (n > MAX_BATCH)
					n = MAX_BATCH;
				Object[] a = new Object[n];
				int j = 0;
				do { a[j] = i.next(); } while (++j < n && i.hasNext());
				batch = j;
				if (est != Long.MAX_VALUE)
					est -= j;
				return new ArraySpliterator<>(a, 0, j, characteristics);
			}
			return null;
		}

		@Override
		public long estimateSize() {
			if (it == null) {
				it = cursor.iterator();
				return est = cursor.size();
			}
			return est;
		}

		@Override
		public int characteristics() {
			return characteristics;
		}

		@Override
		public Comparator<? super T> getComparator() {
			if (hasCharacteristics(Spliterator.SORTED))
				return null;
			throw new IllegalStateException();
		}
	}

	static final class ArraySpliterator<T> implements Spliterator<T> {

		private final Object[] array;
		private int index;        // current index, modified on advance/split
		private final int fence;  // one past last index
		private final int characteristics;

		public ArraySpliterator(Object[] array, int origin, int fence, int additionalCharacteristics) {
			this.array = array;
			this.index = origin;
			this.fence = fence;
			this.characteristics = additionalCharacteristics | Spliterator.SIZED | Spliterator.SUBSIZED;
		}

		@Override
		public Spliterator<T> trySplit() {
			int lo = index, mid = (lo + fence) >>> 1;
			return (lo >= mid)
				? null
				: new ArraySpliterator<>(array, lo, index = mid, characteristics);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void forEachRemaining(Consumer<? super T> action) {
			Object[] a; int i, hi; // hoist accesses and checks from loop
			if (action == null)
				throw new NullPointerException();
			if ((a = array).length >= (hi = fence) &&
				(i = index) >= 0 && i < (index = hi)) {
				do { action.accept((T)a[i]); } while (++i < hi);
			}
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (action == null)
				throw new NullPointerException();
			if (index >= 0 && index < fence) {
				@SuppressWarnings("unchecked") T e = (T) array[index++];
				action.accept(e);
				return true;
			}
			return false;
		}

		@Override
		public long estimateSize() { return fence - index; }

		@Override
		public int characteristics() {
			return characteristics;
		}

		@Override
		public Comparator<? super T> getComparator() {
			if (hasCharacteristics(Spliterator.SORTED))
				return null;
			throw new IllegalStateException();
		}
	}

}
