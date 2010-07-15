#ifndef JOIN_H
#define JOIN_H

template <class ValueInitializer, class Container>
inline typename Container::value_type join(const ValueInitializer& initializer,
                                           const Container& container) {
        typename Container::value_type separator(initializer);
        typename Container::value_type joined;

        if (container.empty())
                return joined;

        typename Container::const_iterator it = container.begin();
        joined = *it;
        ++it;
        while (it != container.end()) {
                joined.insert(joined.end(), separator.begin(), separator.end());
                joined.insert(joined.end(), it->begin(), it->end());
                ++it;
        }

        return joined;
}

#endif
