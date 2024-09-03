import React from 'react';

import type {PropsWithChildren} from 'react';
import PianoSdk, {ENDPOINT} from './piano';

import {
  View,
  Button,
  ScrollView,
  StyleSheet,
  SafeAreaView,
  Text,
  Platform,
} from 'react-native';

type SectionProps = PropsWithChildren<{
  title: string;
}>;

/* AID ufficiale del progetto, attualmente non funziona */
// const AID = 'gQJIZmmgpe';

/* AID di prova, trovato sul web, che fa procedere */
const AID = Platform.OS === 'ios' ? 'DtvhlLYXsu' : 'gQJIZmmgpe';
const FACEBOOK_AID = '617883002025316';

import {Colors} from 'react-native/Libraries/NewAppScreen';

const styles = StyleSheet.create({
  scrollView: {
    backgroundColor: Colors.lighter,
  },
  body: {
    backgroundColor: Colors.white,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
});

class App extends React.Component<any, any> {
  unsubscribe: any;

  constructor(props: any) {
    super(props);
    this.state = {
      data: undefined,
    };
  }

  componentDidMount() {
    PianoSdk.init(AID, ENDPOINT.PRODUCTION_EUROPE, FACEBOOK_AID);
    // this.unsubscribe = PianoSdk.addEventListener(this._onListener);
  }

  componentWillUnmount() {
    // this.unsubscribe();
  }

  _onListener = (response: any) => {
    console.log('====onListener====');
    console.log(response);
  };

  _onShowLoginCallback = (response: any) => {
    console.log('====onShowLoginCallback====');
    console.log(response);
  };

  _onTemplateCallback = (response: any) => {
    console.log('====onTemplateCallback====');
    console.log(response);
  };

  _signIn = () => {
    // PianoSdk.signInDemo();
    PianoSdk.signIn((data: any) => {
      this.setState({
        data,
      });
    });
  };

  _register = () => {
    PianoSdk.register((data: any) => {
      this.setState({
        data,
      });
    });
  };

  _getExperience = () => {
    // accessToken: string
    // contentIsNative: boolean
    // debug: boolean
    // url : string,
    // contentAuthor: string,
    // contentSection: string,
    // customVariables: Object,
    // tag: string,
    // tags: Array<string>,
    // zone: string
    // referer: string
    const config = {
      debug: true,
    };

    PianoSdk.getExperience(
      config,
      this._onShowLoginCallback,
      this._onTemplateCallback,
    );
  };

  _signOut = () => {
    const accessToken = this.state.data ? this.state.data.accessToken : '';
    PianoSdk.signOut(accessToken, () => {
      this.setState({
        data: undefined,
      });
    });
  };

  render() {
    const {data} = this.state;
    return (
      <SafeAreaView>
        <ScrollView
          contentInsetAdjustmentBehavior="automatic"
          style={styles.scrollView}>
          <View style={{backgroundColor: '#1e90ff'}}>
            <Text
              style={{
                textAlign: 'center',
                textAlignVertical: 'center',
                padding: 25,
                fontSize: 30,
                color: '#fff',
              }}>
              BRIDGE Piano.io
            </Text>
          </View>

          <View style={styles.body}>
            {!data ? (
              <View style={styles.sectionContainer}>
                <Button title="Sign In" onPress={this._signIn} />
              </View>
            ) : null}

            {!data ? (
              <View style={styles.sectionContainer}>
                <Button title="Register" onPress={this._register} />
              </View>
            ) : null}

            {data ? (
              <View style={styles.sectionContainer}>
                <Button title="Sign Out" onPress={this._signOut} />
              </View>
            ) : null}

            <View style={styles.sectionContainer}>
              <Button
                title="Execute Experience"
                onPress={this._getExperience}
              />
            </View>
          </View>
        </ScrollView>
      </SafeAreaView>
    );
  }
}

export default App;
