package com.aantik.texteditor;

/*
 * Created by aanti
 * 8/24/2026 5:32 AM
 *
 *   ⋆    ႔ ႔
 *     ᠸ^ ^ ⸝⸝
 *       |、˜〵
 *       じしˍ,)⁐̤ᐷ
 *
 * Fox Mode 🍺
 */

public class ObjectTree<T> {
    private Object[] arrT;

    public ObjectTree(int size) {
        arrT = new Object[size];
    }

    public void sObject(int index, T obj) {
        if (index >= 0 && index < arrT.length) {
            arrT[index] = obj;
        }
    }

    @SuppressWarnings("unchecked")
    public T gObject(int index) {
        if (index >= 0 && index < arrT.length) {
            return (T) arrT[index];
        }
        return null;
    }

    public boolean Can(T obj) {

        for (Object item : arrT) {
            if (item != null && item.equals(obj)) {
                return true;
            }
        }

        return false;
    }

    public T Rep(int index, T mNewObj) {
        if (index >= 0 && index < arrT.length) {
            @SuppressWarnings("unchecked")
            T mRet = (T) arrT[index];
            arrT[index] = mNewObj;
            return mRet;
        }
        return null;
    }
}