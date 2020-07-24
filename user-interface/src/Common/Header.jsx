import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {AppBar} from 'material-ui';
import defaultTheme from '../Utils/Theme';

export default class Header extends Component {
    render() {
        return (
            <AppBar
                style={{zIndex: this.props.theme.zIndex.drawer + 100, position: this.props.position}}
                title={this.props.title}
                iconElementRight={this.props.rightElement}
                iconElementLeft={this.props.logo}
                onLeftIconButtonClick={this.props.onLogoClick}
                iconStyleLeft={{margin: '0 15px 0 0', display: 'flex', alignItems: 'center'}}
                titleStyle={{fontSize: 16}}
                zDepth={2}
            />
        );
    }
}


Header.propTypes = {
    logo: PropTypes.element,
    onLogoClick: PropTypes.func,
    title: PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.element,
    ]).isRequired,
    rightElement: PropTypes.element,
    theme: PropTypes.shape({}),
    position: PropTypes.string
};

Header.defaultProps = {
    logo: <span/>,
    onLogoClick: null,
    rightElement: <span/>,
    theme: defaultTheme,
    title: 'Cybertronez File Search'
};
