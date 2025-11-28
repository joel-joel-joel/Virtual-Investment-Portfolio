import React from 'react';
import { TouchableOpacity, GestureResponderEvent, ViewStyle } from 'react-native';

interface HapticTabProps {
    children: React.ReactNode;
    onPress?: (event: GestureResponderEvent) => void;
    style?: ViewStyle | ViewStyle[];
}

export const HapticTab: React.FC<HapticTabProps> = ({ children, onPress, style }) => {
    return (
        <TouchableOpacity onPress={onPress} style={style} activeOpacity={0.7}>
            {children}
        </TouchableOpacity>
    );
};
