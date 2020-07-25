
import { darkBaseTheme, getMuiTheme, lightBaseTheme } from 'material-ui/styles/index';
import * as Colors from 'material-ui/styles/colors';

export const darkTheme = getMuiTheme(darkBaseTheme,
    {
        name: 'dark',
        palette: {
            primary1Color: Colors.orange800,
            accent1Color: Colors.teal500,
            textColor: Colors.white,
            alternateTextColor: Colors.white,
        },
        appBar: {
            color: Colors.blueGrey900,
            textColor: Colors.grey200,
            height: 40,
        },
        drawer: {
            color: Colors.blueGrey900,
        },
        snackbar: {
            textColor: Colors.black,
        },
    }
);

export const lightTheme = getMuiTheme(lightBaseTheme,
    {
        name: 'light',
        palette: {
            primary1Color: Colors.orange800,
            accent1Color: Colors.teal500,
            textColor: Colors.black,
            alternateTextColor: Colors.black,
        },
        appBar: {
            color: Colors.grey200,
            textColor: Colors.grey900,
            height: 40,
        },
        drawer: {
            color: Colors.grey400,
        },
        snackbar: {
            textColor: Colors.white,
        },
        toggle: {
            trackOffColor: Colors.grey500,
            thumbOffColor: Colors.grey700,
        },
    }
);

export default darkTheme;
