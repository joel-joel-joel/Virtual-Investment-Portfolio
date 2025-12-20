import { openBrowserAsync, WebBrowserPresentationStyle } from 'expo-web-browser';
import { TouchableOpacity, Text, Linking } from 'react-native';
import { type ReactNode } from 'react';

interface ExternalLinkProps {
  href: string;
  children?: ReactNode;
  [key: string]: any;
}

export function ExternalLink({ href, children, ...rest }: ExternalLinkProps) {
  const handlePress = async () => {
    if (process.env.EXPO_OS !== 'web') {
      // On native, open in in-app browser
      await openBrowserAsync(href, {
        presentationStyle: WebBrowserPresentationStyle.AUTOMATIC,
      });
    } else {
      // On web, open in new tab
      Linking.openURL(href);
    }
  };

  return (
    <TouchableOpacity onPress={handlePress} {...rest}>
      {typeof children === 'string' ? <Text>{children}</Text> : children}
    </TouchableOpacity>
  );
}
