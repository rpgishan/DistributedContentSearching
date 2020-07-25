import React, {Component} from 'react';
import {BrowserRouter} from 'react-router-dom';
import {Route, Switch} from 'react-router';

import './App.css';
import fileSearchPage from './Pages/FileSearchPage';
import nodeDistributionPage from './Pages/NodeDistributionPage';

class App extends Component {

    render() {
        return(
            <BrowserRouter>
                <Switch>
                    <Route exact path='/' component={fileSearchPage}/>
                    <Route exact path='/file-search' component={fileSearchPage}/>
                    <Route exact path='/node-distribution' component={nodeDistributionPage}/>
                </Switch>
            </BrowserRouter>
        );
    }
};

export default App;
