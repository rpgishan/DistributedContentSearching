import React, {Component} from 'react';

class FileSearchPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            files: null
        };
    }

    componentDidMount() {

    }

    render() {
        return (<h1>Welcome to search page</h1>);
    }
}

export default (FileSearchPage);